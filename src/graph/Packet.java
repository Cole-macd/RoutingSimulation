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
	
	public Packet(String msg, boolean encrypted){
		this.message = msg;
		this.encrypted = encrypted;
		this.packetSizeBytes += msg.getBytes().length;
		if(encrypted){
			this.encryptedStack = new Stack<byte[]>();
			this.encryptedSessionKeys = new ArrayList<byte[]>();
			this.sessionKey = new byte[128];
			Random r = new Random();
			r.nextBytes(this.sessionKey);
			this.packetSizeBytes += this.sessionKey.length;
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
	
	public String getMessage(){
		if(!this.encrypted){
			return this.message;
		}else{
			return null;
		}
	}
	
	public void addEncryptedValue(byte[] value){
		this.encryptedStack.push(value);
		this.packetSizeBytes += (value.length);
	}
	
	public byte[] getNextEncryptedValueIfValid(Node node){
		boolean valid = false;
		for(int i=0; i < this.encryptedSessionKeys.size(); i++){
			byte[] currentSessionKey = this.encryptedSessionKeys.get(i);			
			byte[] nodeSessionKey = node.decryptMessage(currentSessionKey);
			if(Arrays.equals(nodeSessionKey, this.sessionKey) && node.equals(this.nextRelayNode)){
				valid = true;
				break;
			}
		}
		
		if(valid){
			byte[] retValue = this.encryptedStack.pop();
			this.packetSizeBytes -= (retValue.length);
			return retValue;
		}else{
			return null;
		}
	}
	
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
}
