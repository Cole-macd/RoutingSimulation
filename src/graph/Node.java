package graph;

import java.util.ArrayList;

public class Node {
    private double longitude;
    private double latitude;
    private String key;
    private String name;
    private ArrayList<Edge> links;
    
    public Node(String name, String key, double longitude, double latitude) {
        this.name = name;
        this.setKey(key);
        this.longitude = longitude;
        this.latitude = latitude;
        this.links = new ArrayList<Edge>();
    }
    
    public void addLink(Edge link) {
        this.links.add(link);
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
