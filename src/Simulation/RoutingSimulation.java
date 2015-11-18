package Simulation;

import graph.Graph;
import graph.Node;

import java.util.Scanner;

import graph.Edge;
import graph.GraphHelpers;
import Simulation.ShortestPath;

public class RoutingSimulation {
	private static Graph graph;
	
	public static void main(String[] args){
		graph = new Graph();
		graph.createGraph();
		/*for(int i=0; i < graph.edges.length; i++){
			Node source = graph.edges[i].getFrom();
			Node dest = graph.edges[i].getTo();
			System.out.println("Distance between " + source.getName() + " to " +
							   dest.getName() + " is " + GraphHelpers.getDistance(source, dest));
		}
		*/
		System.out.println("Running shortest path, choose starting node:");
		for(int i=0; i < graph.nodes.length; i++){
			System.out.println(i + ". " + graph.nodes[i].getName());
		}
		int sourceNodeIndex = getIndexFromUser("Starting node number >> ");
		int destNodeIndex = getIndexFromUser("Destination node number >> ");
		
		Node sourceNode = graph.nodes[sourceNodeIndex];
		Node destNode = graph.nodes[destNodeIndex];
		
		System.out.println("Sending message from " + sourceNode.getName() + " to " +
						   destNode.getName());
		
		ShortestPath sp = new ShortestPath();
		sp.startSimulation(sourceNode, destNode);
	}
	
	public static int getIndexFromUser(String prompt){
		int index;
		while(true){
			Scanner reader = new Scanner(System.in);
			System.out.print(prompt);
			try{
				index = reader.nextInt();
			}catch (Exception e){
				System.out.println("Invalid choice");
				continue;
			}
			break;
		}
		return index;
	}
}
