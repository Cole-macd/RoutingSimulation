package Simulation;
import graph.Node;
import graph.Graph;
import graph.Edge;
import graph.GraphHelpers;
import graph.Packet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

public class ShortestPath {
	public final double PROP_SPEED = 200000000.0; 		//in m/s
	private Graph graph;
	
	
	/* Initialize the class */
	public ShortestPath(Graph graph){
		this.graph = graph;
	}
	
	
	/* Computes shortest path from source to destination and sends packet through the path */
	public void startSimulation(Node sourceNode, Node destNode, String message){
		Packet packet = new Packet(message, false);
		
		long processingStartTime = System.nanoTime();

		//compute and get shortest path from source to destination
		computePathsFromSource(sourceNode);
		Node[] shortestPath = getShortestPath(destNode);
		
		long processingEndTime = System.nanoTime();
		double processingDelay = (processingEndTime - processingStartTime) / 1e9;
		double averageProcessingDelay = processingDelay / (double)shortestPath.length;
		double totalTime = processingDelay;
		
		System.out.println("Running shortest path from " + sourceNode.getName() + "(" + sourceNode.getKey() +
						   ") to " + destNode.getName() + "(" + destNode.getKey() + ")");
		printPath(shortestPath);
		
		for(int i=0; i < shortestPath.length-1; i++){
			//send packet from one node to another
			totalTime += sendPacket(packet, shortestPath[i], shortestPath[i+1]);
		}
		System.out.println("Final message received at " + destNode.getName() + " is \"" + message + "\". The message took " + formatSeconds(totalTime));
		System.out.println("The average processing delay at each node was " + formatSeconds(averageProcessingDelay));
	}
	
	
	/* Simulates sending a packet from one node to another assuming there is a link between the two.
	 * Also simulates transmission and propagation delays */
	public double sendPacket(Packet packet, Node source, Node dest){
		//get the size of the packet, different for encrypted (onion routing) and unencrypted (shortest path)
		int messageDataSizeBytes = packet.getPacketSize();
		int messageOverheadBytes = 32;
		int messageSizeBytes = messageDataSizeBytes + messageOverheadBytes;

		//compute the geographic distance between the two nodes, in meters
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
		System.out.println(source.getName() + " to " + dest.getName() +
						   " took " + formatSeconds(thisLinkTime));
		return thisLinkTime;
	}
	
	
	/* Assuming the shortest paths have been computed by the source, return an array of nodes in the 
	 * shortest path to the destination.
	 */
	public Node[] getShortestPath(Node dest){
		ArrayList<Node> path = new ArrayList<Node>();
		
		//start at the destination and iterate through the previous nodes set when computing shortest path
		for (Node n = dest; n != null; n = n.previousNode){
			path.add(n);
		}
		
		//reverse the list so that the first element is the source node
		Collections.reverse(path);
		Node[] li = new Node[path.size()];
		
		//return the path in array form
		return path.toArray(li);
	}
	
	
	/* Performs Dijkstra's algorithm to compute the shortest paths from the source node */
	public void computePathsFromSource(Node source){
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
	
	/* Prints the input path node by node */
	public void printPath(Node[] path){
		System.out.println("Shortest path:");
		for(int i=0; i < path.length; i++){
			System.out.print(path[i].getName() + "(" + path[i].getKey() + ")");
			if(i != path.length-1){
				System.out.print(" --> ");
			}else{
				System.out.println("");
			}
		}
	}
}
