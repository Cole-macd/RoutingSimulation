package graph;

import java.util.ArrayList;

public class Node implements Comparable<Node> {
    private double longitude;
    private double latitude;
    public double minDistance = Double.POSITIVE_INFINITY;
    public Node previousNode;
    private int key;
    private String name;
    private ArrayList<Edge> outgoingEdgeObjects;
    private Integer[] outgoingEdgeKeys;
    
    public Node(int key, String name, double latitude, double longitude, Integer[] edges) {
        this.name = name;
        this.setKey(key);
        this.longitude = longitude;
        this.latitude = latitude;
        this.outgoingEdgeObjects = new ArrayList<Edge>();
        this.outgoingEdgeKeys = edges;
    }
    
    public int compareTo(Node other){
    	return Double.compare(minDistance, other.minDistance);
    }
    
    public Integer[] getEdgeKeys(){
    	return this.outgoingEdgeKeys;
    }
    
    public void addEdgeObject(Edge link) {
        this.outgoingEdgeObjects.add(link);
    }
    
    public Edge[] getEdgeObjects(){
    	Edge[] edgeList = new Edge[outgoingEdgeObjects.size()];
    	return outgoingEdgeObjects.toArray(edgeList);
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}
