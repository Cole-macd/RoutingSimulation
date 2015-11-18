package Simulation;

import graph.Graph;

public class RoutingSimulation {
	private static Graph graph;
	
	public static void main(String[] args){
		graph = new Graph();
		graph.createGraph();
		
		for(int i=0; i < graph.nodes.length; i++){
			System.out.println("Node " + graph.nodes[i].getName() + " at " + graph.nodes[i].getLatitude());
		}
	}
}
