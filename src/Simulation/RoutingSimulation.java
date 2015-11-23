package Simulation;

import graph.Graph;
import graph.Node;

import java.util.Scanner;

import graph.Edge;
import graph.GraphHelpers;
import Simulation.ShortestPath;

public class RoutingSimulation {
	private enum ROUTING_TYPE{ ONION, NORMAL};
	
	public static void main(String[] args){
		Graph graph = new Graph();
		graph.createGraph();
		
		ROUTING_TYPE t = getRoutingTypeFromUser();
		Node[] endPoints = getEndPointsFromUser(graph);
		String message = getMessageFromUser();
		
		System.out.print("Sending message \"" + message + "\" from " +
						   endPoints[0].getName() + " to " + endPoints[1].getName() +
						   " using the ");
		
		if(t == ROUTING_TYPE.ONION){
			System.out.print("onion routing protocol\n");
			OnionRouting or = new OnionRouting(graph);
			or.startSimulation(endPoints[0], endPoints[1], message);
		}else{
			System.out.print("shortest path protocol\n");
			ShortestPath sp = new ShortestPath(graph);
			sp.startSimulation(endPoints[0], endPoints[1], message);
		}
		
		/*display distance of every edge
		for(int i=0; i < graph.edges.length; i++){
			Node source = graph.edges[i].getFrom();
			Node dest = graph.edges[i].getTo();
			System.out.println("Distance between " + source.getName() + " to " +
							   dest.getName() + " is " + GraphHelpers.getDistance(source, dest));
		}*/
		
	}
	
	public static Node[] getEndPointsFromUser(Graph graph){
		String display = "\nChoose start and end nodes to send the message.\n";
		for(int i=0; i < graph.nodes.length; i++){
			display += (i + ". " + graph.nodes[i].getName());
			if(i != graph.nodes.length-1){
				display += "\n";
			}
		}
		String startIndexString = getUserInput(display, "Starting node number >> ", true, false);
		String endIndexString = getUserInput(display, "Destination node number >> ", false, false);
		int startIndex = Integer.parseInt(startIndexString);
		int endIndex = Integer.parseInt(endIndexString);
		
		Node[] result = {graph.nodes[startIndex], graph.nodes[endIndex]};
		return result;
	}
	
	public static ROUTING_TYPE getRoutingTypeFromUser(){
		String display = ("Choose what type of routing simulation you want to perform:\n" +
						 "0. Onion routing\n1. Shortest path");
		String prompt = ("Routing type number >> ");
		String valueString = getUserInput(display, prompt, true, false);
		int value = Integer.parseInt(valueString);
		if(value == 0){
			return ROUTING_TYPE.ONION;
		}else{
			return ROUTING_TYPE.NORMAL;
		}
	}
	
	public static String getMessageFromUser(){
		String display = "\nEnter the message to send between nodes:";
		String prompt = ">> ";
		return getUserInput(display, prompt, true, true);
	}
	
	public static String getUserInput(String display, String prompt, Boolean showDisplay, Boolean stringInput){
		String value;
		if(showDisplay){
			System.out.println(display);
		}
		while(true){
			Scanner reader = new Scanner(System.in);
			System.out.print(prompt);
			try{
				if(stringInput){
					value = reader.nextLine();
				}else{
					Integer tempValue = reader.nextInt();
					value = tempValue.toString();
				}
			}catch (Exception e){
				System.out.println("Invalid choice");
				continue;
			}
			break;
		}
		return value;
	}
}
