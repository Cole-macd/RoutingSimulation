package Simulation;

import graph.Graph;
import graph.Node;
import graph.Packet;
import Simulation.ShortestPath;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Random;
import javax.crypto.Cipher;

public class OnionRouting {
    public Graph graph;
    public final String dataSplitValue1 = ";";      //used to split encrypted data
    public final String dataReplaceValue1 = "~";
    public final String dataSplitValue2 = ":";      //used to split encrypted data
    public final String dataReplaceValue2 = "%";
    public Boolean verbose = true;
    
    public OnionRouting(Graph graph, Boolean verbose){
        this.graph = graph;
        this.verbose = verbose;
    }
    
    /* Initializes the packet, chooses the random relay nodes, and starts the protocol */
    public double[] startSimulation(Node source, Node destination, String message){
        Node[] relayNodes = getRelayNodes(source, destination);
        Packet packet = initializePacket(message, relayNodes, destination);
        return startOnionRouter(source, packet, relayNodes[0]);
    }
    
    /* Start protocol; the packet only knows the location of the next relay node */
    public double[] startOnionRouter(Node source, Packet packet, Node entryNode){
        //start processing timer to process first node, to compute forwarding tables for nodes in path
        long processingStartTime = System.nanoTime();
        ShortestPath sp = new ShortestPath(this.graph, this.verbose);
        sp.computeForwardingTables(source, entryNode);
        
        //end processing timer
        long processingEndTime = System.nanoTime();
        double processingDelay = (processingEndTime - processingStartTime) / 1e9;
        double processingDelaySum = processingDelay;
        double totalTime = processingDelay;
        
        //display the endpoints of this leg of the path
        if(this.verbose){
            System.out.println("\nComputing shortest path from " + source.getName() + "(" + source.getKey() +
                       ") to " + entryNode.getName() + "(" + entryNode.getKey() + ")");
            sp.printPath(source, entryNode);
        }
        
        Node currentNode = source;
        Node nextDest = entryNode;
        Node nextNode = source.getNextNodeFromForwardingTable(nextDest);
        byte[] tempEncryptedValue;
        byte[] tempDecryptedValue;
        String tempDecryptedString = "";
        boolean finalTrip = false;
        String finalMessage = "";
        double nodeCount = 0;
        while(true){
            //send packet to to next relay node, or destination
            while(nextNode != null){
                totalTime += sp.sendPacketSingleLink(packet, currentNode, nextNode);
                nodeCount++;
                currentNode = nextNode;
                
                //process intermediate node to get next node in path, and add to timing totals
                totalTime += sp.simulateTraffic();
                processingStartTime = System.nanoTime();
                nextNode = currentNode.getNextNodeFromForwardingTable(nextDest);
                processingEndTime = System.nanoTime();
                processingDelay = (processingEndTime - processingStartTime) / 1e9;
                processingDelaySum += processingDelay;
                totalTime += processingDelay;
            }
            
            //packet has reached relay node, so start processing timer
            currentNode = nextDest;
            processingStartTime = System.nanoTime();
            
            //get next layer of encrypted data from stack, null if invalid relay node
            tempEncryptedValue = packet.getNextEncryptedValueIfValid(currentNode);
            
            if(tempEncryptedValue != null){
                //node is a relay node, so decrypt the layer removed
                tempDecryptedValue = currentNode.decryptMessage(tempEncryptedValue);
                tempDecryptedString = new String(tempDecryptedValue);
                
                //process the decrypted data to get location of next relay node, or of destination for exit node
                String[] decryptedData = processPacketData(tempDecryptedString);
                Integer nextKey = Integer.parseInt(decryptedData[0]);
                nextDest = getNodeFromKey(nextKey);
                
                //compute and get the shortest path from current node to next relay node
                sp.computeForwardingTables(currentNode, nextDest);
                
                //get next node in path to relay node
                nextNode = currentNode.getNextNodeFromForwardingTable(nextDest);
                
                //if current node is the exit node, set the final boolean to true
                if(decryptedData.length > 1){
                    finalTrip = true;
                    finalMessage = decryptedData[1];
                }
                
                //end the processing timer
                processingEndTime = System.nanoTime();
                processingDelay = (processingEndTime - processingStartTime) / 1e9;
                processingDelaySum += processingDelay;
                totalTime += processingDelay;

                if(this.verbose){
                    //print results of this leg of the transmission
                    System.out.println(currentNode.getName() + "(" + currentNode.getKey() + 
                               ") recognized it was a relay node; removed and decrypted data layer, data:\"" + 
                               tempDecryptedString + "\" processed in " + sp.formatSeconds(processingDelay));
                    
                    //print endpoints for next leg of transmission
                    System.out.println("\nComputing shortest path from " + currentNode.getName() + "(" + currentNode.getKey() +
                                       ") to " + nextDest.getName() + "(" + nextDest.getKey() + ")");
                    sp.printPath(currentNode, nextDest);
                }
            }else if(finalTrip){
                //at this point, currentNode is the destination, must process the data, so end the processing timer and break loop
                nodeCount++;
                processingEndTime = System.nanoTime();
                processingDelay= (processingEndTime - processingStartTime) / 1e9;
                processingDelaySum += processingDelay;
                totalTime += processingDelay;
                break;
            }
        }
        
        //compute average processing delay and print results of transmission
        double averageProcessingDelay = processingDelaySum / (double)nodeCount;
        if(this.verbose){
            System.out.println("Final message received at " + currentNode.getName() + " was \"" + finalMessage + "\" in " + sp.formatSeconds(totalTime));
            System.out.println("Nodes traversed: " + (int)nodeCount + ", Average processing delay: " + sp.formatSeconds(averageProcessingDelay));
        }
        double[] retList = {(double)nodeCount, totalTime, averageProcessingDelay};
        return retList;
    }
    
    public String[] processPacketData(String decryptedString){
        //process the decrypted data to determine the next relay node
        String[] valueList = decryptedString.split(dataSplitValue1);
        String[] dataList = valueList[0].split(dataSplitValue2);
        String nextKey = dataList[1];
        String[] retValue;
        if(valueList.length > 1){
            //current node is final relay node, so set next "relay node" is the destination
            String[] messageList = valueList[1].split(dataSplitValue2);
            
            //replace the placeholder characters with the chars from the original message (eg my~message%is --> my;message:is)
            String finalMessage = messageList[1].replace(dataReplaceValue1, dataSplitValue1).replace(dataReplaceValue2, dataSplitValue2);
            retValue = new String[2];
            retValue[0] = nextKey;
            retValue[1] = finalMessage.replace(dataReplaceValue1, dataSplitValue1);
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
        
        //replace any instances of ";" or ":" in original message, since those are the chars used for splitting encrypted data
        String messageToStore = message.replace(dataSplitValue1, dataReplaceValue1);
        messageToStore = messageToStore.replace(dataSplitValue2, dataReplaceValue2);
        String nextMessage = "next:" + dest.getKey() + dataSplitValue1 + "message:" + messageToStore;
        
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
                if(this.verbose){
                    System.out.println(graph.nodes[i].getName() + "(" + graph.nodes[i].getKey() + ") chosen as relay node.");
                }
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
