package Simulation;
import graph.Node;
import graph.Graph;
import graph.Edge;
import graph.GraphHelpers;
import graph.Packet;

import java.util.PriorityQueue;
import java.util.Random;

public class ShortestPath {
    public final double PROP_SPEED = 800000000.0;       //in m/s
    private Graph graph;
    public Boolean verbose = true;
    
    
    /* Initialize the class */
    public ShortestPath(Graph graph, Boolean verbose){
        this.graph = graph;
        this.verbose = verbose;
    }
    
    
    /* Computes shortest path from source to destination and sends packet through the path */
    public double[] startSimulation(Node sourceNode, Node destNode, String message){
        Packet packet = new Packet(message, false);
        
        //start processing timer to process first node
        long processingStartTime = System.nanoTime();

        //compute forwarding tables for the shortest path between the source and dest
        computeForwardingTables(sourceNode, destNode);
        if(this.verbose){
            printPath(sourceNode, destNode);
        }
        
        //end processing timer, add to totalTime and total processing delay
        long processingEndTime = System.nanoTime();
        double processingDelay = (processingEndTime - processingStartTime) / 1e9;
        double processingDelaySum = processingDelay;
        double totalTime = processingDelay;
        
        //display the endpoints of the transmission
        if(this.verbose){
            System.out.println("Running shortest path from " + sourceNode.getName() + "(" + sourceNode.getKey() +
                                ") to " + destNode.getName() + "(" + destNode.getKey() + ")");
        }
        
        //send packet along path using forwarding table of each node on the path
        int nodeCount = 0;
        Node currentNode = sourceNode;
        Node nextNode = sourceNode.getNextNodeFromForwardingTable(destNode);
        while(nextNode != null){
            //send the packet along one link
            nodeCount++;
            totalTime += sendPacketSingleLink(packet, currentNode, nextNode);
            currentNode = nextNode;
            
            //process the packet to get next node, and add to the processing time count
            totalTime += simulateTraffic();
            processingStartTime = System.nanoTime();
            nextNode = currentNode.getNextNodeFromForwardingTable(destNode);
            processingEndTime = System.nanoTime();
            processingDelay = ((processingEndTime - processingStartTime) / 1e9);
            processingDelaySum += processingDelay;
            totalTime += processingDelay;
        }
        
        //need to include processing time at destination
        nodeCount++;
        
        //computer average processing delay per node
        double averageProcessingDelay = processingDelaySum / (double)nodeCount;

        //print simulation results
        if(this.verbose){
            System.out.println("Final message received at " + destNode.getName() + " is \"" + message + "\". The message took " + formatSeconds(totalTime));
            System.out.println("The average processing delay at each node was " + formatSeconds(averageProcessingDelay));
        }
        double[] retList = {nodeCount, totalTime, averageProcessingDelay};
        return retList;
    }
    
    
    /* Simulates sending a packet from one node to another assuming there is a link between the two.
     * Also simulates transmission and propagation delays */
    public double sendPacketSingleLink(Packet packet, Node source, Node dest){
        //get the size of the packet, different for encrypted (onion routing) and unencrypted (shortest path)
        int messageDataSizeBytes = packet.getPacketSize();
        int messageOverheadBytes = 32;
        int messageSizeBytes = messageDataSizeBytes + messageOverheadBytes;

        //compute the geographic distance between the two nodes, in meters
        //not considered processing time, because distance just used to calculation transmission time (packet or node doesn't calculate this in practice)
        double nextDistance = GraphHelpers.getDistance(source, dest);
        
        //get the speed of the link connecting the two nodes and compute the delays
        Edge nextEdge = getNextEdge(source, dest);
        double transmissionTime = ((float)messageSizeBytes)/nextEdge.getLink_speed();
        double propagationTime = nextDistance / PROP_SPEED;
        
        //total delay to send the packet (not including processing delay)
        double thisLinkTime = transmissionTime + propagationTime;
        
        //simulate the delay by sleeping the program for the link time
        try {
            Thread.sleep((long) (thisLinkTime * 1000));                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        
        //display the result of the transmission
        if(this.verbose){
            System.out.println(source.getName() + " to " + dest.getName() +
                                " took " + formatSeconds(thisLinkTime));
        }
        return thisLinkTime;
    }
    
    
    /* Performs Dijkstra's algorithm to compute the shortest paths from the source node. 
     * Once the shortest path is computed, add an entry to the forwarding table for each node
     * telling the current node the next node in the path to the destination.
     */
    public void computeForwardingTables(Node source, Node dest){
        //since this may be called multiple times for different sources in onion routing, need
        //to reset the previousNode and minDistance attributes of each node
        for(int i=0; i < this.graph.nodes.length; i++){
            this.graph.nodes[i].previousNode = null;
            this.graph.nodes[i].minDistance = Double.POSITIVE_INFINITY;
        }

        //initialize priority queue and add source
        source.minDistance = 0.0;
        PriorityQueue<Node> nodeQ = new PriorityQueue<Node>();
        nodeQ.add(source);
        
        while(!nodeQ.isEmpty()){
            Node u = nodeQ.poll();
            
            //search every edge connected to u
            for (Edge e : u.getEdgeObjects()){
                //compute the geographic distance between nodes u and v, and set this aas the edge weight
                Node v = e.getTo();
                double weight = GraphHelpers.getDistance(u, v);
                double distanceThroughU = u.minDistance + weight;
                if(distanceThroughU < v.minDistance){
                    //u is closer to v than v's current previous node, so set v's pervious to u
                    nodeQ.remove(v);
                    v.minDistance = distanceThroughU;
                    v.previousNode = u;
                    nodeQ.add(v);
                }
            }
        }
        
        //use dijkstra's previous nodes to construct forwarding table to destination
        Node prev = dest;
        for (Node n = dest; n != null; n = n.previousNode){
            //path.add(n);  
            if(n != dest){
                n.addForwardingTableEntry(dest, prev);
            }else{
                n.addForwardingTableEntry(dest, null);
            }
            prev = n;
        }
    }
    
    
    /* Searches the edges connected to the source and returns the edge linking to the destination */
    public Edge getNextEdge(Node source, Node dest){
        Edge[] sourceEdges = source.getEdgeObjects();
        for(int i=0; i < sourceEdges.length; i++){
            if(sourceEdges[i].getTo() == dest){
                return sourceEdges[i];
            }
        }
        return null;
    }
    
    
    /* Simulates the queueing delay experienced by a node */
    public double simulateTraffic(){
        //generate random queuing delay between 0 and 5ms
        Random rand = new Random();
        double qDelay = rand.nextInt(5);
        //simulate the delay by sleeping the program for the link time
        try {
            Thread.sleep((long) (qDelay));                 
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return qDelay / 1000;       //qDelay is in ms
    }
    
    
    /* Formats the input to 3 decimal points, and adds the correct units for readability */
    public String formatSeconds(double timeSeconds){
        if(timeSeconds >= 0.1){
            return String.format("%.3f",timeSeconds) + "s";
        }else if(timeSeconds >= 0.0001){
            return String.format("%.3f",(timeSeconds * 1000.0)) + "ms";
        }else if(timeSeconds >= 0.0000001){
            return String.format("%.3f",(timeSeconds * 1000.0 * 1000.0)) + "us";
        }else{
            return String.format("%.3f",(timeSeconds * 1000.0 * 1000.0 * 1000.0))+ "ns";
        }
    }
    
    /* Prints the path from source to dest using forwarding tables*/
    public void printPath(Node source, Node dest){
        System.out.println("Shortest path:");
        Node currentNode = source;
        while(currentNode != dest){
            System.out.print(currentNode.getName() + "(" + currentNode.getKey() + ") --> ");
            currentNode = currentNode.getNextNodeFromForwardingTable(dest);
        }
        System.out.println(currentNode.getName() + "(" + currentNode.getKey() + ")");

    }
}
