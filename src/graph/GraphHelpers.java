package graph;

import java.lang.Math;

import graph.Node;

public class GraphHelpers {
    private static final double R = 6372.8;
    
    public static double getDistance(Node from, Node to) {
        // Use Haversine distance formula
        
        double d_lat = Math.toRadians(to.getLatitude() - from.getLatitude());
        double d_lon = Math.toRadians(to.getLongitude() - from.getLongitude());
        double from_latitude = Math.toRadians(from.getLatitude());
        double to_latitude = Math.toRadians(to.getLatitude());
        
        double a = Math.pow(Math.sin(d_lat / 2), 2) + Math.pow(Math.sin(d_lon / 2), 2) * Math.cos(from_latitude) * Math.cos(to_latitude);
        double c = 2 * Math.asin(Math.sqrt(a));
        
        return R * c;
    }
}
