package Simulation;

import graph.Graph;
import graph.Node;

import java.util.Scanner;

import graph.Edge;
import graph.GraphHelpers;
import Simulation.ShortestPath;

public class RoutingSimulation {
	private enum ROUTING_TYPE{ONION,NORMAL};
	
	public static void main(String[] args){
		Graph graph = new Graph();
		graph.createGraph();
		
		//get simulation parameters from the user
		ROUTING_TYPE t = getRoutingTypeFromUser();
		Node[] endPoints = getEndPointsFromUser(graph);
		String message = getMessageFromUser();
		
		//display the simulation parameters
		System.out.print("Sending message \"" + message + "\" from " +
						   endPoints[0].getName() + " to " + endPoints[1].getName() +
						   " using the ");
		int totalTime = 0;
		int totalAvgDelay = 0;
		int totalPathLength = 0;
		int[] results = new int[3];
		Boolean verbose = false;
		if(t == ROUTING_TYPE.ONION){
			//initialize onion routing simulation
			System.out.print("onion routing protocol\n");
			OnionRouting or = new OnionRouting(graph, verbose);
			for (int i = 0; i < 10000; i++) {
			    results = or.startSimulation(endPoints[0], endPoints[1], message);
			    totalPathLength += results[0];
			    totalTime += results[1];
			    totalAvgDelay += results[2];
			}
			
			totalPathLength /= 10000;
			totalAvgDelay /= 10000;
			totalTime /= 10000;
			System.out.println("Avg path length is: " + totalPathLength);
			System.out.println("Avg delay is: " + totalAvgDelay);
			System.out.println("Avg time is: " + totalTime);
		}else{
			//initialize shortest path simulation
		    System.out.print("shortest path protocol\n");
		    ShortestPath sp = new ShortestPath(graph, verbose);
            for (int i = 0; i < 10000; i++) {
                results = sp.startSimulation(endPoints[0], endPoints[1], message);
                totalPathLength += results[0];
                totalTime += results[1];
                totalAvgDelay += results[2];
            }
            
            totalPathLength /= 10000;
            totalAvgDelay /= 10000;
            totalTime /= 10000;
            System.out.println("Avg path length is: " + totalPathLength);
            System.out.println("Avg delay is: " + totalAvgDelay);
            System.out.println("Avg time is: " + totalTime);
		}
		
		/*display distance of every edge
		for(int i=0; i < graph.edges.length; i++){
			Node source = graph.edges[i].getFrom();
			Node dest = graph.edges[i].getTo();
			System.out.println("Distance between " + source.getName() + " to " +
							   dest.getName() + " is " + GraphHelpers.getDistance(source, dest));
		}*/
	}
	
	/* Polls the user to get the index of the source and destination nodes of the transmission */
	public static Node[] getEndPointsFromUser(Graph graph){
		String display = "\nChoose start and end nodes to send the message.\n";
		int[] validInputs = new int[graph.nodes.length];
		
		//add the node names and keys to the display string
		for(int i=0; i < graph.nodes.length; i++){
			display += (i + ". " + graph.nodes[i].getName());
			if(i != graph.nodes.length-1){
				display += "\n";
			}
			validInputs[i] = i;
		}
		
		//get the nodes
		String startIndexString = getUserInput(display, "Starting node number >> ", true, false, validInputs);
		String endIndexString = getUserInput(display, "Destination node number >> ", false, false, validInputs);
		int startIndex = Integer.parseInt(startIndexString);
		int endIndex = Integer.parseInt(endIndexString);
		
		//return the nodes
		Node[] result = {graph.nodes[startIndex], graph.nodes[endIndex]};
		return result;
	}
	
	/* Poll the user to get what routing protocol to use */
	public static ROUTING_TYPE getRoutingTypeFromUser(){
		String display = ("Choose what type of routing simulation you want to perform:\n" +
						 "0. Onion routing\n1. Shortest path");
		String prompt = ("Routing type number >> ");
		int[] valids = {0,1};
		String valueString = getUserInput(display, prompt, true, false, valids);
		int value = Integer.parseInt(valueString);
		if(value == 0){
			return ROUTING_TYPE.ONION;
		}else{
			return ROUTING_TYPE.NORMAL;
		}
	}
	
	/* Poll the user to get the message to be sent from the source to the destination */
	public static String getMessageFromUser(){
		String display = "\nEnter the message to send between nodes:";
		String prompt = ">> ";
		int[] dummy = {};
		return getUserInput(display, prompt, true, true, dummy);
	}
	
	/* Ask the user for input, and dont proceed until input is valid */
	public static String getUserInput(String display, String prompt, Boolean showDisplay, Boolean stringInput, int[] validValues){
		String value = "";
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
					boolean validInput = false;
					for(int i=0; i < validValues.length; i++){
						if(tempValue == validValues[i]){
							value = tempValue.toString();
							validInput = true;
							break;
						}
					}
					if(!validInput){
						System.out.println("Invalid choice");
						continue;
					}
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
