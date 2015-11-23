package graph;

import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

public class Node implements Comparable<Node> {
    private double longitude;
    private double latitude;
    public double minDistance = Double.POSITIVE_INFINITY;
    public Node previousNode;
    private int key;
    private String name;
    private ArrayList<Edge> outgoingEdgeObjects;
    private Integer[] outgoingEdgeKeys;
    private PublicKey publicRSAKey;
    private PrivateKey privateRSAKey;
    private IvParameterSpec keySpec;
    
    public Node(int key, String name, double latitude, double longitude, Integer[] edges) {
        this.name = name;
        this.setKey(key);
        this.longitude = longitude;
        this.latitude = latitude;
        this.outgoingEdgeObjects = new ArrayList<Edge>();
        this.outgoingEdgeKeys = edges;
        SecureRandom randomObj = new SecureRandom();
        keySpec = new IvParameterSpec(randomObj.generateSeed(16));
    }
    
    public IvParameterSpec getKeySpec(){
    	return keySpec;
    }
    
    public void generateRSAEncryptionKeys(){
    	try{
    	      final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    	      keyGen.initialize(2048);
    	      final KeyPair key = keyGen.generateKeyPair();
    	      this.publicRSAKey = key.getPublic();
    	      this.privateRSAKey = key.getPrivate();
    	}catch (Exception e) {
    	      e.printStackTrace();
    	}
    }
    
    public PublicKey getRSAPublicKey(){
    	return this.publicRSAKey;
    }
    
    public byte[] decryptMessage(byte[] message){
    	byte[] decryptedText = null;
    	try{
    		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    		cipher.init(Cipher.DECRYPT_MODE, this.privateRSAKey);
    		//byte[] messageBytes = message.getBytes();
    		decryptedText = cipher.doFinal(message);
    	}catch (Exception e){
    		return null;
    	}
    	return decryptedText;
    	/*
    	try {
			return new String(decryptedText, "UTF8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}*/
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
