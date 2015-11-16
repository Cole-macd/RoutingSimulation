package graph;

import graph.Node;

public class Edge {
    private Node from;
    private Node to;
    private int link_speed; // in Mbps
    
    public Edge(Node from, Node to, int link_speed) {
        this.from = from;
        this.to = to;
        this.link_speed = link_speed;
    }
    
    public Node getFrom() {
        return from;
    }
    
    public void setFrom(Node from) {
        this.from = from;
    }

    public Node getTo() {
        return to;
    }

    public void setTo(Node to) {
        this.to = to;
    }

    public double getLink_speed() {
        return link_speed;
    }

    public void setLink_speed(int link_speed) {
        this.link_speed = link_speed;
    }
}
