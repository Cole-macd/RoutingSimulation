package Simulation;

import graph.Node;

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
		byte[] tempMessageBytes = message.getBytes();
		
		/*relayNodes[0].generateRSAEncryptionKeys();
		String tempMessage = encryptMessage(message, relayNodes[0].getRSAPublicKey());
		System.out.println("After 1 encryption, message is \"" + tempMessage+ "\"");
		
		tempMessage = relayNodes[0].decryptMessage(tempMessage);
		System.out.println("After 1 decrpytions, message is \"" + tempMessage + "\"");
		*/
		
		
		/*System.out.println("\nInitial message: \"" + tempMessageBytes.toString() + "\"");
		for(int i=0; i < relayNodes.length; i++){
			System.out.println(relayNodes[i].getKey() + " generating keys");
			relayNodes[i].generateRSAEncryptionKeys();
			tempMessageBytes = encryptMessage(tempMessageBytes, relayNodes[i].getRSAPublicKey());
			System.out.println("After " + (i+1) + " encryptions, message is \"" + tempMessageBytes.toString() + "\"");
		}
		
		for(int i=relayNodes.length-1; i >= 0; i--){
			tempMessageBytes = relayNodes[i].decryptMessage(tempMessageBytes);
			System.out.println("After " + (3-i) + " decrpytions, message is \"" + tempMessageBytes.toString() + "\"");
		}*/
		
		/*ArrayList<Key> symKeys = new ArrayList<Key>();
		Key[] keyList = new Key[3];
		String tempMessage = message;
		for(int i=relayNodes.length-1; i>= 0; i--){
			Key tempKey = generateSymmetricKey();
			keyList[i] = tempKey;
			SecureRandom random = new SecureRandom();
		    IvParameterSpec iv = new IvParameterSpec(random.generateSeed(16));
		    tempMessage = encryptWithSymKey(tempMessage, tempKey, iv);
		    System.out.println("After " + (3-i) + " encryptions, string is " + tempMessage);
		}
		
		for(int i=0; i < relayNodes.length; i++){
			SecureRandom random = new SecureRandom();
		    IvParameterSpec iv = new IvParameterSpec(random.generateSeed(16));
			tempMessage = decryptWithSymKey(tempMessage, keyList[i], iv);
		    System.out.println("After " + i + " decryptions, string is " + tempMessage);

		}
		*/
		Key tempKey = generateSymmetricKey();
	    IvParameterSpec iv = relayNodes[0].getKeySpec();
	    String firstEncryptedValue = encryptWithSymKey(message, tempKey, iv);
	    System.out.println("after 1 encryption, message is " + firstEncryptedValue);
	    
	    Key tempKey2 = generateSymmetricKey();
	    IvParameterSpec iv2 = relayNodes[1].getKeySpec();
	    String secondEncryptedValue = encryptWithSymKey(firstEncryptedValue, tempKey2, iv2);
	    System.out.println("after 2 encryptions, message is " + secondEncryptedValue);
	    
	    String firstDecryptedValue = decryptWithSymKey(secondEncryptedValue, tempKey2, iv2);
	    System.out.println("after 1 decryption, message is " + firstDecryptedValue);

	    
	    String secondDecryptedValue = decryptWithSymKey(firstDecryptedValue, tempKey, iv);
	    System.out.println("after 2 decryptions, message is " + secondDecryptedValue);
		
		
		
		
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
	
	public String encryptWithSymKey(String message, Key key, IvParameterSpec iv){
		try{
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	      cipher.init(Cipher.ENCRYPT_MODE,key,iv);

	      byte[] stringBytes = message.getBytes();

	      byte[] raw = cipher.doFinal(stringBytes);

	      return new BASE64Encoder().encode(raw);
	      //return Base64.encodeBase64String(raw);
	      //return new String(raw);
	      //return raw;
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
	
	public String encryptMessage(String message, PublicKey key){
		byte[] cipherText = null;
		try{
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] messageBytes = message.getBytes();
			cipherText = cipher.doFinal(messageBytes);
		}catch (Exception e){
			e.printStackTrace();
		}
		try {
			return new String(cipherText, "UTF8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
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
