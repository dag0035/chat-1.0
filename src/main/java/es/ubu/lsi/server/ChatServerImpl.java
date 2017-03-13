package es.ubu.lsi.server;

import java.text.SimpleDateFormat;

import es.ubu.lsi.common.ChatMessage;

public class ChatServerImpl implements ChatServer{

	public final int DEFAULT_PORT = 1500;
	public int clientId;
	public SimpleDateFormat sdf;
	public int port;
	public boolean alive;
	
	public ChatServerImpl(int port){
		this.port = port;
	}
	
	public void stratup(){
		
	}
	
	public void shutdown(){
		
	}
	
	public void broadcast(ChatMessage message){
		
	}
	
	public void remove(int id){
		
	}
	
	public static void main(String[] args) {
		
	}
	
}

class ServerThreadForClient implements Runnable{

	public void run() {
		// TODO Auto-generated method stub
		
	}

}
