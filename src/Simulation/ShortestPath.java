package Simulation;
import graph.Node;
import graph.Edge;
import graph.GraphHelpers;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

public class ShortestPath {
	public final double PROP_SPEED = 200000000.0; //m/s
	
	public void startSimulation(Node sourceNode, Node destNode, String message){
		computePathsFromSource(sourceNode);
		Node[] shortestPath = getShortestPath(destNode);
		System.out.println("\nShortest path from " + sourceNode.getName() + " to " + destNode.getName() + ":");
		for(Node p: shortestPath){
			System.out.print(p.getName() + "(" + p.getKey() + ") --> ");
		}
		System.out.println("\n");
		sendPacket(shortestPath, message);
	}
	
	public void sendPacket(Node[] pathList, String message){
		int messageSizeBytes = 0;
		int messageOverheadBytes = 32;
		try {
			final byte[] utf8Bytes = message.getBytes("UTF-8");
			messageSizeBytes = utf8Bytes.length;
		} catch (UnsupportedEncodingException e) {
			messageSizeBytes = message.length();
		}
		messageSizeBytes += messageOverheadBytes;
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
		System.out.println("Final message received: " + message + " in time " + formatSeconds(totalTime));
	}
	
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
			path.add(n);
		}
		Collections.reverse(path);
		Node[] li = new Node[path.size()];
		return path.toArray(li);
	}
	
	public void computePathsFromSource(Node source){
		//dijkstras
		source.minDistance = 0.0;
		
		PriorityQueue<Node> nodeQ = new PriorityQueue<Node>();
		nodeQ.add(source);
		
		while(!nodeQ.isEmpty()){
			Node u = nodeQ.poll();
			
			for (Edge e : u.getEdgeObjects()){
				Node v = e.getTo();
				double weight = GraphHelpers.getDistance(u, v);
				double distanceThroughU = u.minDistance + weight;
				
				if(distanceThroughU < v.minDistance){
					nodeQ.remove(v);
					v.minDistance = distanceThroughU;
					v.previousNode = u;
					nodeQ.add(v);
				}
			}
		}
	}
	
}
