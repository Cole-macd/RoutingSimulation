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
	private String sessionKeyString;
	private ArrayList<byte[]> encryptedSessionKeys;
	
	public Packet(String msg){
		this.message = message;
		this.encryptedStack = new Stack<byte[]>();
		this.encryptedSessionKeys = new ArrayList<byte[]>();
		sessionKey = new byte[128];
		Random r = new Random();
		r.nextBytes(sessionKey);
		
		sessionKey = "test".getBytes();
		
		sessionKeyString = new String(sessionKey);
	}
	
	public void addEncryptedValue(byte[] value){
		this.encryptedStack.push(value);
	}
	
	public byte[] getNextEncryptedValueIfValid(Node node){
		boolean valid = false;
		for(int i=0; i < this.encryptedSessionKeys.size(); i++){
			byte[] currentSessionKey = this.encryptedSessionKeys.get(i);			
			byte[] nodeSessionKey = node.decryptMessage(currentSessionKey);
			if(Arrays.equals(nodeSessionKey, this.sessionKey)){
				valid = true;
				break;
			}
		}
		
		if(valid){
			return this.encryptedStack.pop();
		}else{
			System.out.println("Node " + node.getKey() + " is not authorized to access encrypted data");
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
	}
}
