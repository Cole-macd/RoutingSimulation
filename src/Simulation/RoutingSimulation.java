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
