package graph;

import graph.Node;

import graph.Edge;

public class Graph {
	public Node[] nodes;
	public Edge[] edges;
	
	public void createGraph(){
    	Node n0 = new Node("New York", "0", -79.0662, 43.08342);	//0
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
    	Edge e1a = new Edge(n0, n8, linkSpeed);		//edge 0-11
    	Edge e1b = new Edge(n8, n0, linkSpeed);
    	Edge e2a = new Edge(n1, n9, linkSpeed);		//edge 3-12
    	Edge e2b = new Edge(n9, n1, linkSpeed);
    	Edge e3a = new Edge(n1, n2, linkSpeed);		//edge 3-5
    	Edge e3b = new Edge(n2, n1, linkSpeed);
    	Edge e4a = new Edge(n2, n3, linkSpeed);		//edge 5-6
    	Edge e4b = new Edge(n3, n2, linkSpeed);
    	Edge e5a = new Edge(n3, n4, linkSpeed);		//edge 6-7
    	Edge e5b = new Edge(n4, n3, linkSpeed);
    	Edge e6a = new Edge(n4, n5, linkSpeed);		//edge 7-8
    	Edge e6b = new Edge(n5, n4, linkSpeed);
    	Edge e7a = new Edge(n4, n7, linkSpeed); 	//edge 7-10
    	Edge e7b = new Edge(n7, n4, linkSpeed);
    	Edge e8a = new Edge(n5, n6, linkSpeed);		//edge 8-9
    	Edge e8b = new Edge(n6, n5, linkSpeed);
    	Edge e9a = new Edge(n6, n7, linkSpeed);		//edge 9-10
    	Edge e9b = new Edge(n7, n6, linkSpeed);
    	
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

    	int numEdges = 20;
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
	}
}
