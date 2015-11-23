package Simulation;
import graph.Node;
import graph.Graph;
import graph.Edge;
import graph.GraphHelpers;
import graph.Packet;

import java.io.UnsupportedEncodingException;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

public class ShortestPath {
	public final double PROP_SPEED = 200000000.0; //m/s
	private Graph graph;
	
	public ShortestPath(Graph graph){
		this.graph = graph;
	}
	
	public void startSimulation(Node sourceNode, Node destNode, String message){
		Packet packet = new Packet(message, false);
		
		computePathsFromSource(sourceNode);
		Node[] shortestPath = getShortestPath(destNode);
		
		//sendPacket(shortestPath, packet);
	}
	
	public void sendPacket(Packet packet, Node source, Node dest){
		int messageDataSizeBytes = packet.getPacketSize();
		int messageOverheadBytes = 32;
		int messageSizeBytes = messageDataSizeBytes + messageOverheadBytes;
		double totalTime = 0.0;

		double nextDistance = GraphHelpers.getDistance(source, dest);
		Edge nextEdge = getNextEdge(source, dest);
		double transmissionTime = ((float)messageSizeBytes)/nextEdge.getLink_speed();
		double propagationTime = nextDistance / PROP_SPEED;
		double thisLinkTime = transmissionTime + propagationTime;
		totalTime += thisLinkTime;
		try {
		    Thread.sleep((long) (thisLinkTime * 1000));                 //1000 milliseconds is one second.
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
		System.out.println(source.getName() + " to " + dest.getName() +
						   " took " + formatSeconds(thisLinkTime));

	}
	
	/*
	public void sendPacket(Node[] pathList, Packet packet){
		int messageDataSizeBytes = packet.getPacketSize();
		int messageOverheadBytes = 32;
		int messageSizeBytes = messageDataSizeBytes + messageOverheadBytes;
		
		double totalTime = 0.0;
		for(int i=0; i < pathList.length-1; i++){
			Node currentNode = pathList[i];
			Node nextNode = pathList[i+1];
			
			double nextDistance = GraphHelpers.getDistance(currentNode, nextNode);
			Edge nextEdge = getNextEdge(currentNode, nextNode);
			double transmissionTime = ((float)messageSizeBytes)/nextEdge.getLink_speed();
			double propagationTime = nextDistance / PROP_SPEED;
			double thisLinkTime = transmissionTime + propagationTime;
			totalTime += thisLinkTime;
			try {
			    Thread.sleep((long) (thisLinkTime * 1000));                 //1000 milliseconds is one second.
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			System.out.println(currentNode.getName() + " to " + nextNode.getName() +
							   " took " + formatSeconds(thisLinkTime));
		}
		//System.out.println("Final message received: \"" + message + "\" in time " + formatSeconds(totalTime));
	}*/
	
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
	
	public Edge getNextEdge(Node source, Node dest){
		Edge[] sourceEdges = source.getEdgeObjects();
		for(int i=0; i < sourceEdges.length; i++){
			if(sourceEdges[i].getTo() == dest){
				return sourceEdges[i];
			}
		}
		return null;
	}
	
	public Node[] getShortestPath(Node dest){
		ArrayList<Node> path = new ArrayList<Node>();
		for (Node n = dest; n != null; n = n.previousNode){
			//System.out.println("Node:" + n.getKey() + "," + n.getName());
			path.add(n);
		}
		Collections.reverse(path);
		Node[] li = new Node[path.size()];
		return path.toArray(li);
	}
	
	public void computePathsFromSource(Node source){
		for(int i=0; i < this.graph.nodes.length; i++){
			this.graph.nodes[i].previousNode = null;
			this.graph.nodes[i].minDistance = Double.POSITIVE_INFINITY;
		}
		//System.out.println("setting paths from " + source.getKey());
		//dijkstras
		source.minDistance = 0.0;
		
		PriorityQueue<Node> nodeQ = new PriorityQueue<Node>();
		nodeQ.add(source);
		
		while(!nodeQ.isEmpty()){
			Node u = nodeQ.poll();
			
			for (Edge e : u.getEdgeObjects()){
				Node v = e.getTo();
				//System.out.println("iterating edges for " + u.getKey() + ", looking at " + v.getKey());
				double weight = GraphHelpers.getDistance(u, v);
				double distanceThroughU = u.minDistance + weight;
				
				if(distanceThroughU < v.minDistance){
					//System.out.println("Setting " + v.getKey() + "'s previous to " + u.getKey());
					nodeQ.remove(v);
					v.minDistance = distanceThroughU;
					v.previousNode = u;
					//System.out.println("adding " + v.getKey() + " to queue");
					nodeQ.add(v);
				}else{
					//System.out.println("didnt set cause " + v.minDistance + " is less than " + distanceThroughU);
				}
			}
		}
	}
	
}
