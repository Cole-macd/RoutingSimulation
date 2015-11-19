package graph;

import graph.Node;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import graph.Edge;

public class Graph {
	public Node[] nodes;
	public Edge[] edges;

	
	public void createGraph(){
		Map<Integer, Node> nodeMap = new HashMap<Integer, Node>();
		ArrayList<Node> tempNodes = new ArrayList<Node>();
		ArrayList<Edge> tempEdges = new ArrayList<Edge>();
		
		//add node objects
		File cityFile = new File("./src/Cities.txt");
		try (BufferedReader br = new BufferedReader(new FileReader(cityFile))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		        String[] elements = line.split(" ");
		        List<Integer> edgeArrayList = new ArrayList<>();
		        
		        //add the integer keys of nodes connected
		        for(int i=4; i < elements.length; i++){
		        	edgeArrayList.add(Integer.parseInt(elements[i]));
		        }
		        
		        //convert array list to list to store in node
		        Integer[] edgeList = new Integer[edgeArrayList.size()];
		        edgeList = edgeArrayList.toArray(edgeList);
		        Node temp = new Node(Integer.parseInt(elements[0]), elements[1], 
		        					 Double.parseDouble(elements[2]), Double.parseDouble(elements[3]),
		        					 edgeList);
		        
		        //eventual nodes variable, needs to be array list during generation
		        tempNodes.add(temp);
		        
		        //dict of node keys to node objects, to add edges later
		        nodeMap.put(Integer.parseInt(elements[0]), temp);
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//store in class variable
		this.nodes = new Node[tempNodes.size()];
		this.nodes = tempNodes.toArray(this.nodes);
		
		int linkSpeed = 40000000;
		
		//once all node objects created, can add edge objects
		for(int i=0; i < this.nodes.length; i++){
			//for each node, add one edge object for each adjacency
			Integer[] currentNodeNeighbors = this.nodes[i].getEdgeKeys();
			Node currentNode = this.nodes[i];
			for(int j=0; j < currentNodeNeighbors.length; j++){
				Node destNode = nodeMap.get(currentNodeNeighbors[j]);
				Edge tempEdge = new Edge(currentNode, destNode, linkSpeed);
				tempEdges.add(tempEdge);
				currentNode.addEdgeObject(tempEdge);
			}
		}
		
		this.edges = new Edge[tempEdges.size()];
		this.edges = tempEdges.toArray(this.edges);
		for(int i=0; i < this.nodes.length; i++){
			Node n = this.nodes[i];
			Edge[] tempEdgeList = n.getEdgeObjects();
			System.out.println("Node " + n.getName() + ":");
			for(int j=0; j < tempEdgeList.length; j++){
				System.out.println("Edge to " + tempEdgeList[j].getTo().getName());
			}
			System.out.println("\n");
		}
		
		
		//Map<String, List<String>> map = new HashMap<String, List<String>>();
		//format = "city name", {"key", "longitude", "latitude", "attached city1","ac2"....}
		//map.put("Vancouver", ("-79","42"));
		//THESE LATITUDE/LONGITUDE VALUES ARE INCORRECT
    	/*Node n0 = new Node("New York", "0", -79.0662, 43.08342);	//0
    	Node n1 = new Node("Moncton", "1", -64.8018, 46.11594);		//3
    	Node n2 = new Node("Edmundsten", "2", -68.32512, 47.3737);	//5
    	Node n3 = new Node("Quebec", "3", -71.21454, 46.81228);		//6
    	Node n4 = new Node("Montreal", "4", -73.58781, 45.50884);	//7
    	Node n5 = new Node("Toronto", "5", -79.4163, 43.70011);		//8
    	Node n6 = new Node("Buffalo", "6", -108.48475, 55.85017);	//9
    	Node n7 = new Node("Albany", "7", -63.64872, 46.28343);		//10
    	Node n8 = new Node("Boston", "8", -121.44399, 49.87002);	//11
    	Node n9 = new Node("Halifax", "9", -63.57333, 44.646);		//12
    	
    	int linkSpeed = 40000000;	//??
    	
    	Edge e0a = new Edge(n0, n7, linkSpeed);		//edge 0-10
    	Edge e0b = new Edge(n7, n0, linkSpeed);
    	n0.addLink(e0a);
    	n0.addLink(e0b);
    	n7.addLink(e0a);
    	n7.addLink(e0b);
    	Edge e1a = new Edge(n0, n8, linkSpeed);		//edge 0-11
    	Edge e1b = new Edge(n8, n0, linkSpeed);
    	n0.addLink(e1a);
    	n0.addLink(e1b);
    	n8.addLink(e1a);
    	n8.addLink(e1b);
    	Edge e2a = new Edge(n1, n9, linkSpeed);		//edge 3-12
    	Edge e2b = new Edge(n9, n1, linkSpeed);
    	n1.addLink(e2a);
    	n1.addLink(e2b);
    	n9.addLink(e2a);
    	n9.addLink(e2b);
    	Edge e3a = new Edge(n1, n2, linkSpeed);		//edge 3-5
    	Edge e3b = new Edge(n2, n1, linkSpeed);
    	n1.addLink(e3a);
    	n1.addLink(e3b);
    	n2.addLink(e3a);
    	n2.addLink(e3b);
    	Edge e4a = new Edge(n2, n3, linkSpeed);		//edge 5-6
    	Edge e4b = new Edge(n3, n2, linkSpeed);
    	n2.addLink(e4a);
    	n2.addLink(e4b);
    	n3.addLink(e4a);
    	n3.addLink(e4b);
    	Edge e5a = new Edge(n3, n4, linkSpeed);		//edge 6-7
    	Edge e5b = new Edge(n4, n3, linkSpeed);
    	n3.addLink(e5a);
    	n3.addLink(e5b);
    	n4.addLink(e5a);
    	n4.addLink(e5b);
    	Edge e6a = new Edge(n4, n5, linkSpeed);		//edge 7-8
    	Edge e6b = new Edge(n5, n4, linkSpeed);
    	n4.addLink(e6a);
    	n4.addLink(e6b);
    	n5.addLink(e6a);
    	n5.addLink(e6b);
    	Edge e7a = new Edge(n4, n7, linkSpeed); 	//edge 7-10
    	Edge e7b = new Edge(n7, n4, linkSpeed);
    	n4.addLink(e7a);
    	n4.addLink(e7b);
    	n7.addLink(e7a);
    	n7.addLink(e7b);
    	Edge e8a = new Edge(n5, n6, linkSpeed);		//edge 8-9
    	Edge e8b = new Edge(n6, n5, linkSpeed);
    	n5.addLink(e8a);
    	n5.addLink(e8b);
    	n6.addLink(e8a);
    	n6.addLink(e8b);
    	Edge e9a = new Edge(n6, n7, linkSpeed);		//edge 9-10
    	Edge e9b = new Edge(n7, n6, linkSpeed);
    	n6.addLink(e9a);
    	n6.addLink(e9b);
    	n7.addLink(e9a);
    	n7.addLink(e9b);
    	Edge e10a = new Edge(n2, n6, linkSpeed);
    	Edge e10b = new Edge(n6, n2, linkSpeed);
    	n2.addLink(e10a);
    	n2.addLink(e10b);
    	n6.addLink(e10a);
    	n6.addLink(e10b);
    	Edge e11a = new Edge(n2, n0, linkSpeed);
    	Edge e11b = new Edge(n0, n2, linkSpeed);
    	n2.addLink(e11a);
    	n2.addLink(e11b);
    	n0.addLink(e11a);
    	n0.addLink(e11b);
    	Edge e12a = new Edge(n2, n8, linkSpeed);
    	Edge e12b = new Edge(n8, n2, linkSpeed);
    	n2.addLink(e12a);
    	n2.addLink(e12b);
    	n8.addLink(e12a);
    	n8.addLink(e12b);
    	
    	int numNodes = 10;
    	nodes = new Node[numNodes];
    	nodes[0] = n0;
    	nodes[1] = n1;
    	nodes[2] = n2;
    	nodes[3] = n3;
    	nodes[4] = n4;
    	nodes[5] = n5;
    	nodes[6] = n6;
    	nodes[7] = n7;
    	nodes[8] = n8;
    	nodes[9] = n9;

    	int numEdges = 26;
    	edges = new Edge[numEdges];
    	
    	edges[0] = e0a;
    	edges[1] = e1a;
    	edges[2] = e2a;
    	edges[3] = e3a;
    	edges[4] = e4a;
    	edges[5] = e5a;
    	edges[6] = e6a;
    	edges[7] = e7a;
    	edges[8] = e8a;
    	edges[9] = e9a;
    	edges[10] = e0b;
    	edges[11] = e1b;
    	edges[12] = e2b;
    	edges[13] = e3b;
    	edges[14] = e4b;
    	edges[15] = e5b;
    	edges[16] = e6b;
    	edges[17] = e7b;
    	edges[18] = e8b;
    	edges[19] = e9b;
    	edges[20] = e10a;
    	edges[21] = e10b;
    	edges[22] = e11a;
    	edges[23] = e11b;
    	edges[24] = e12a;
    	edges[25] = e12b;*/
	}
}
