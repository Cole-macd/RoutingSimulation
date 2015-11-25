package Simulation;

import graph.Node;
import graph.Packet;
import Simulation.ShortestPath;

import java.security.Key;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import graph.Graph;

public class OnionRouting {
	public Graph graph;
	
	public OnionRouting(Graph graph){
		this.graph = graph;
	}
	
	/* Initializes the packet, chooses the random relay nodes, and starts the protocol */
	public void startSimulation(Node source, Node destination, String message){
		Node[] relayNodes = getRelayNodes(source, destination);
		Packet packet = initializePacket(message, relayNodes, destination);
		startOnionRouter(source, packet);
	}
	
	/* Start protocol; the packet only knows the location of the next relay node */
	public void startOnionRouter(Node source, Packet packet){
		//compute the shortest paths to all nodes from the source
		ShortestPath sp = new ShortestPath(this.graph);
		sp.computePathsFromSource(source);
		
		//compute the shortest path from the source to the first relay node: the entry node
		Node currentNode = source;
		Node nextNode = packet.getNextRelayNode();
		Node[] pathList = sp.getShortestPath(nextNode);
		System.out.println("\nComputing shortest path from " + currentNode.getName() + "(" + currentNode.getKey() +
				   ") to " + nextNode.getName() + "(" + nextNode.getKey() + ")");

		sp.printPath(pathList);
		
		//the protocol parameters
		int currentIndex = 0;
		byte[] tempEncryptedValue;
		byte[] tempDecryptedValue;
		String tempDecryptedString = "";
		boolean finalTrip = false;
		String finalMessage = "";
		double totalTime = 0.0;
		
		while(true){
			//try to remove a layer of encryption, since node doesn't know if it is a relay or not
			currentNode = pathList[currentIndex];
			tempEncryptedValue = packet.getNextEncryptedValueIfValid(currentNode);
			
			if(tempEncryptedValue != null){
				//node is a relay node, so decrypt a layer of packet data
				tempDecryptedValue = currentNode.decryptMessage(tempEncryptedValue);
				tempDecryptedString = new String(tempDecryptedValue);
				
				
				long processingStartTime = System.nanoTime();
				String[] decryptedData = processPacket(tempDecryptedString);
				Integer nextKey = Integer.parseInt(decryptedData[0]);
				if(decryptedData.length > 1){
					finalTrip = true;
					finalMessage = decryptedData[1];
				}
				long processingEndTime = System.nanoTime();
				double processingDelay = (processingEndTime - processingStartTime) / 1e9;
				totalTime += processingDelay;
				System.out.println(currentNode.getName() + "(" + currentNode.getKey() + 
						   ") recognized it was a relay node; removed and decrypted data layer, data:\"" + 
						   tempDecryptedString + "\" processed in " + sp.formatSeconds(processingDelay));
				
				//set the next relay node
				Node nextRelayNode = getNodeFromKey(nextKey);
				packet.setNextRelayNode(nextRelayNode);
				
				//compute and get the shortest path from current node to next relay node
				sp.computePathsFromSource(currentNode);
				pathList = sp.getShortestPath(nextRelayNode);
				System.out.println("\nComputing shortest path from " + currentNode.getName() + "(" + currentNode.getKey() +
								   ") to " + nextRelayNode.getName() + "(" + nextRelayNode.getKey() + ")");
				sp.printPath(pathList);
				
				//reset the path index and send first packet in path
				currentIndex = 0;
				nextNode = pathList[currentIndex+1];
				currentIndex++;
				totalTime += sp.sendPacket(packet, currentNode, nextNode);
			}else{
				//node is not a relay node, so just send the packet to the next node in path
				if(finalTrip && currentIndex == pathList.length-1){
					//the packet has reached the destination
					break;
				}
				nextNode = pathList[currentIndex+1];
				currentIndex++;
				totalTime += sp.sendPacket(packet, currentNode, nextNode);	
			}
		}
		System.out.println("Final message received at " + currentNode.getName() + " was \"" + finalMessage + "\" in " + sp.formatSeconds(totalTime));
	}
	
	public String[] processPacket(String decryptedString){
		//process the decrypted data to determine the next relay node
		String[] valueList = decryptedString.split(",");
		String[] dataList = valueList[0].split(":");
		String nextKey = dataList[1];
		String[] retValue;
		if(valueList.length > 1){
			//current node is final relay node, so set next "relay node" is the destination
			String[] messageList = valueList[1].split(":");
			String finalMessage = messageList[1];
			retValue = new String[2];
			retValue[0] = nextKey;
			retValue[1] = finalMessage;
		}else{
			retValue = new String[1];
			retValue[0] = nextKey;
		}
		return retValue;
	}
	
	/* Initialize the packet by encrypting the locations of the relay nodes and storing them
	 * in a stack, such that the top of the stack holds the entry node, and the bottom of the stack
	 * holds the destination node and the unencrypted message.
	 */
	public Packet initializePacket(String message, Node[] relayNodes, Node dest){
		Packet packet = new Packet(message, true);
		
		//the first thing in the stack is the destination node and unencrypted message
		relayNodes[2].generateRSAEncryptionKeys();
		String nextMessage = "next:" + dest.getKey() + ",message:" + message;
		
		//encrypt the message and store it in the packet's stack
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
		
		//encrypt the packet's session key with each relay node's public key
		//only these nodes will be able to remove encrypted layers
		for(int i=0; i < relayNodes.length; i++){
			packet.storeEncryptedSessionKey(relayNodes[i].getRSAPublicKey());
		}
		
		//set the packets next relay node to the entry node
		packet.setNextRelayNode(relayNodes[0]);
		return packet;
	}
	
	/* Randomly generates 3 nodes in the network to act as relay nodes */
	public Node[] getRelayNodes(Node source, Node dest){
		Random rand = new Random();
		int[] nodeKeys = {-1,-1,-1};
		int currentIndex = 0;
		while(true){
			//generate random number
			int temp = rand.nextInt(graph.nodes.length);
			
			//if the key is not the source, dest, or one of the previously chosen relay nodes, it is valid
			if(temp != source.getKey() && temp != dest.getKey() && temp != nodeKeys[0]
			   && temp != nodeKeys[1] && temp != nodeKeys[2]){
				nodeKeys[currentIndex] = temp;
				currentIndex++;
				if(currentIndex == 3){
					break;
				}
			}
		}
		
		//sort the keys so that there is no bias in how the nodes are chosen
		Arrays.sort(nodeKeys);
		Node[] relayNodes = new Node[3];
		currentIndex = 0;
		for(int i=0; i < graph.nodes.length; i++){
			if(graph.nodes[i].getKey() == nodeKeys[currentIndex]){
				relayNodes[currentIndex] = graph.nodes[i];
				System.out.println(graph.nodes[i].getName() + "(" + graph.nodes[i].getKey() + ") chosen as relay node.");
				currentIndex++;
				if(currentIndex == 3){
					break;
				}
			}
		}
		return relayNodes;
	}
	
	/* Encrypts a string message using the input public key */
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
	}
	
	/* Returns the node objects from the integer key, null if key doesn't exist */
	public Node getNodeFromKey(int key){
		for(int i=0; i < this.graph.nodes.length; i++){
			if(this.graph.nodes[i].getKey() == key){
				return this.graph.nodes[i];
			}
		}
		return null;
	}
}
