package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

public class ChatClientImpl implements ChatClient {

	public String server;
	public String username;
	public int port;
	public boolean carryOn = true;
	public int id;
	
	public ChatClientImpl (String server, int port, String username){
		this.server = server;
		this.port = port;
		this.username = username;
	}
	
	public boolean start(){
		//TODO 
		return true;
	}
	
	public void sendMessage( ChatMessage msg){
		//TODO
	}
	
	public void desconnect(){
		//TODO
	}
	
	public static void main(String[] args) {
		
	}
	
	
	
}

class ChatClientListener implements Runnable {

	public void run() {
		// TODO Auto-generated method stub
		
	}

}
