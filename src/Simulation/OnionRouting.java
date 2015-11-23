package Simulation;

import graph.Node;
import graph.Packet;
import Simulation.ShortestPath;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import graph.Edge;
import graph.Graph;

public class OnionRouting {
	public Graph graph;
	
	public OnionRouting(Graph graph){
		this.graph = graph;
	}
	
	public void startSimulation(Node source, Node destination, String message){
		Node[] relayNodes = getRelayNodes(source, destination);
		Packet packet = initializePacket(message, relayNodes, destination);
		startOnionRouter(source, packet);
		
		/*
		ShortestPath sp = new ShortestPath(this.graph);
		sp.computePathsFromSource(source);
		Node[] pathList = sp.getShortestPath(relayNo);
		sp.sendPacket(pathList, packet);

		packet = sendPacketToEntryNode(packet, source, relayNodes[0]);
		byte[] nextValue = packet.getNextEncryptedValueIfValid(relayNodes[0]);
		byte[] decryptedValue = relayNodes[0].decryptMessage(nextValue);
		String decryptedValueString = new String(decryptedValue);
		String[] dataList = decryptedValueString.split(":");
		Integer nextKey = Integer.parseInt(dataList[1]);*/
		
		//System.out.println("first decrypted value is " + decryptedValueString);
		
		/*
		byte[] nextValue = packet.getNextEncryptedValueIfValid(relayNodes[0]);
		byte[] decryptedValue = relayNodes[0].decryptMessage(nextValue);
		String decryptedValueString = new String(decryptedValue);
		System.out.println("first decrypted value is " + decryptedValueString);
		
		nextValue = packet.getNextEncryptedValueIfValid(relayNodes[1]);
		decryptedValue = relayNodes[1].decryptMessage(nextValue);
		decryptedValueString = new String(decryptedValue);
		System.out.println("second decrypted value is " + decryptedValueString);
		
		nextValue = packet.getNextEncryptedValueIfValid(relayNodes[2]);
		decryptedValue = relayNodes[2].decryptMessage(nextValue);
		decryptedValueString = new String(decryptedValue);
		System.out.println("third decrypted is " + decryptedValueString);*/

	}
	
	public void startOnionRouter(Node source, Packet packet){
		ShortestPath sp = new ShortestPath(this.graph);
		sp.computePathsFromSource(source);
		Node currentNode = source;
		Node nextNode = packet.getNextRelayNode();
		Node[] pathList = sp.getShortestPath(nextNode);
		
		System.out.println("\nShortest path from " + source.getName() + " to " + nextNode.getName() + ":");
		for(Node p: pathList){
			System.out.print(p.getName() + "(" + p.getKey() + ") --> ");
		}
		System.out.println("\n");
		
		int currentIndex = 0;
		int currentRelayIndex = 0;
		byte[] tempEncryptedValue;
		byte[] tempDecryptedValue;
		String tempDecryptedString = "";
		boolean finalTrip = false;
		String finalMessage = "";
		while(true){
			currentNode = pathList[currentIndex];
			tempEncryptedValue = packet.getNextEncryptedValueIfValid(currentNode);
			if(tempEncryptedValue != null){
				System.out.println(currentNode.getKey() + " decrypted successfully");
				tempDecryptedValue = currentNode.decryptMessage(tempEncryptedValue);
				tempDecryptedString = new String(tempDecryptedValue);
				System.out.println(tempDecryptedString);
				String[] valueList = tempDecryptedString.split(",");
				String[] dataList = valueList[0].split(":");
				Integer nextKey = Integer.parseInt(dataList[1]);
				if(valueList.length > 1){
					//final relay node
					finalTrip = true;
					String[] messageList = valueList[1].split(":");
					finalMessage = messageList[1];
				}
				
				for(int i=0; i < this.graph.nodes.length; i++){
					if(nextKey == this.graph.nodes[i].getKey()){
						System.out.println("Computing paths from " + currentNode.getKey() + " to " + this.graph.nodes[i].getKey());
						packet.setNextRelayNode(this.graph.nodes[i]);
						sp.computePathsFromSource(currentNode);
						pathList = sp.getShortestPath(this.graph.nodes[i]);
					}
				}
				/*Node nextRelayNode = getNodeFromKey(nextKey);
				packet.setNextRelayNode(nextRelayNode);
				
				System.out.println("Computing paths from " + currentNode.getKey() + " to " + nextRelayNode.getKey());
				sp = new ShortestPath(this.graph);
				sp.computePathsFromSource(currentNode);
				pathList = sp.getShortestPath(nextRelayNode);*/
				
				for(Node p: pathList){
					System.out.print(p.getName() + "(" + p.getKey() + ") --> ");
				}
				System.out.println("\n");
				currentIndex = 0;
				nextNode = pathList[currentIndex+1];
				currentIndex++;
				sp.sendPacket(packet, currentNode, nextNode);
			}else{
				if(finalTrip && currentIndex == pathList.length-1){
					break;
				}
				nextNode = pathList[currentIndex+1];
				currentIndex++;
				sp.sendPacket(packet, currentNode, nextNode);	
			}
		}
		System.out.println("Final message " + finalMessage);
	}
	
	public Node getNodeFromKey(int key){
		for(int i=0; i < this.graph.nodes.length; i++){
			if(this.graph.nodes[i].getKey() == key){
				return this.graph.nodes[i];
			}
		}
		return null;
	}

	
	public Packet initializePacket(String message, Node[] relayNodes, Node dest){
		Packet packet = new Packet(message, true);
		
		relayNodes[2].generateRSAEncryptionKeys();
		String nextMessage = "next:" + dest.getKey() + ",message:" + message;
		byte[] tempMessage = encryptMessage(nextMessage, relayNodes[2].getRSAPublicKey());
		packet.addEncryptedValue(tempMessage);
		
		relayNodes[1].generateRSAEncryptionKeys();
		nextMessage = "next:" + relayNodes[2].getKey();
		tempMessage = encryptMessage(nextMessage, relayNodes[1].getRSAPublicKey());
		packet.addEncryptedValue(tempMessage);
		
		relayNodes[0].generateRSAEncryptionKeys();
		nextMessage = "next:" + relayNodes[1].getKey();
		tempMessage = encryptMessage(nextMessage, relayNodes[0].getRSAPublicKey());
		packet.addEncryptedValue(tempMessage);
		
		//only allow nodes who have a valid session key to access the encrypted data
		for(int i=0; i < relayNodes.length; i++){
			packet.storeEncryptedSessionKey(relayNodes[i].getRSAPublicKey());
		}
		
		packet.setNextRelayNode(relayNodes[0]);
		
		return packet;
	}
	
	public Key generateSymmetricKey(){
		try{
			KeyGenerator kg = KeyGenerator.getInstance("AES");
		    SecureRandom random = new SecureRandom();
		    kg.init(random);
		    return kg.generateKey();
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public byte[] encryptWithSymKey(String message, Key key, IvParameterSpec iv){
		try{
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	      cipher.init(Cipher.ENCRYPT_MODE,key,iv);

	      byte[] stringBytes = message.getBytes();

	      byte[] raw = cipher.doFinal(stringBytes);

	      //return new BASE64Encoder().encode(raw);
	      //return Base64.encodeBase64String(raw);
	      //return new String(raw);
	      return raw;
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public String decryptWithSymKey(String message, Key key, IvParameterSpec iv){
		try{
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	      cipher.init(Cipher.DECRYPT_MODE, key,iv);

	      byte[] raw = new BASE64Decoder().decodeBuffer(message);

	      byte[] stringBytes = cipher.doFinal(raw);

	      String clearText = new String(stringBytes, "UTF8");
	      return clearText;
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public byte[] encryptMessage(String message, PublicKey key){
		byte[] cipherText = null;
		try{
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] messageBytes = message.getBytes();
			cipherText = cipher.doFinal(messageBytes);
		}catch (Exception e){
			e.printStackTrace();
		}
		return cipherText;
		/*
		try {
			return new String(cipherText, "UTF8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}*/
	}
	
	public Node[] getRelayNodes(Node source, Node dest){
		Random rand = new Random();
		int[] nodeKeys = {-1,-1,-1};
		int currentIndex = 0;
		while(true){
			int temp = rand.nextInt(graph.nodes.length);
			if(temp != source.getKey() && temp != dest.getKey() && temp != nodeKeys[0]
			   && temp != nodeKeys[1] && temp != nodeKeys[2]){
				nodeKeys[currentIndex] = temp;
				System.out.println("adding " + temp + " as a relay");
				currentIndex++;
				if(currentIndex == 3){
					break;
				}
			}
		}
		Arrays.sort(nodeKeys);
		Node[] relayNodes = new Node[3];
		currentIndex = 0;
		for(int i=0; i < graph.nodes.length; i++){
			if(graph.nodes[i].getKey() == nodeKeys[currentIndex]){
				relayNodes[currentIndex] = graph.nodes[i];
				currentIndex++;
				if(currentIndex == 3){
					break;
				}
			}
		}
		return relayNodes;
	}
}
