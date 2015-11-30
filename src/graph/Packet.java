package graph;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

import javax.crypto.Cipher;

public class Packet {
	private String message;
	private Stack<byte[]> encryptedStack;
	private byte[] sessionKey;
	private boolean encrypted;
	private ArrayList<byte[]> encryptedSessionKeys;
	private int packetSizeBytes = 0;
	private Node nextRelayNode;
	
	/* Initialize the packet */
	public Packet(String msg, boolean encrypted){
		this.message = msg;
		this.encrypted = encrypted;
		this.packetSizeBytes += msg.getBytes().length;
		
		if(encrypted){
			//initialize class encryption values
			this.encryptedStack = new Stack<byte[]>();
			this.encryptedSessionKeys = new ArrayList<byte[]>();
			
			//a single session key will be used for one packet (1 packet = 1 session)
			this.sessionKey = new byte[128];
			Random r = new Random();
			r.nextBytes(this.sessionKey);
			this.packetSizeBytes += this.sessionKey.length;
		}
	}
	
	//Add an encrypted layer onto the stack of packet data
	public void addEncryptedValue(byte[] value){
		this.encryptedStack.push(value);
		this.packetSizeBytes += (value.length);
	}
	
	/* Each relay node will encrypt the session key with their own public key.
	 * This will later be used to verify whether a node is a designated relay node.
	 */
	public void storeEncryptedSessionKey(PublicKey key){
		byte[] cipherText = null;
		try{
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			cipherText = cipher.doFinal(sessionKey);
		}catch (Exception e){
			e.printStackTrace();
		}
		encryptedSessionKeys.add(cipherText);
		this.packetSizeBytes += (cipherText.length);
	}
	
	/* Attempt to remove a layer of encryption by decrypting the valid encrypted session keys.
	 * That is, each relay node will have encrypted the session key, using its public key, and stored in the packet's list.
	 * Thus, each node will call this function to see if there is any data for them to read.
	 * This function verifies that the node is a relay node by decrypting every encrypted session key.
	 * If the input node is a relay node, then one of these decryptions should return the original session key,
	 * meaning that this relay node can decrypt the next layer of data.
	 */
	public byte[] getNextEncryptedValueIfValid(Node node){
		boolean valid = false;
		
		//search through the list of session keys encrypted by designated relay nodes
		for(int i=0; i < this.encryptedSessionKeys.size(); i++){
			byte[] currentSessionKey = this.encryptedSessionKeys.get(i);
			
			//decrypt this encrypted session key with the node's private key
			byte[] nodeSessionKey = node.decryptMessage(currentSessionKey);
			
			//if the decrypted value equals the packet's session key, then this node is allowed to decrypt a data layer
			//second half of if statement to confirm that only the next relay node, and not just any relay node in the path, can decrypt the next layer
			if(Arrays.equals(nodeSessionKey, this.sessionKey)){
				valid = true;
				this.encryptedSessionKeys.remove(i);
				break;
			}
		}
		if(valid){
			//node allowed to decrypt a layer of data
			byte[] retValue = this.encryptedStack.pop();
			
			//update the packet size to remove the layer of encrypted data from consideration
			this.packetSizeBytes -= (retValue.length);
			return retValue;
		}else{
			//not a valid relay node, cannot remove any layers of encryption
			return null;
		}
	}
	
	/* Only returns the string message if the packet is not encrypted */
	public String getMessage(){
		if(!this.encrypted){
			return this.message;
		}else{
			return null;
		}
	}
	
	public boolean isEncrypted(){
		return this.encrypted;
	}
	
	public void setNextRelayNode(Node n){
		this.nextRelayNode = n;
	}
	
	public Node getNextRelayNode(){
		return this.nextRelayNode;
	}
	
	public int getPacketSize(){
		return this.packetSizeBytes;
	}
}
