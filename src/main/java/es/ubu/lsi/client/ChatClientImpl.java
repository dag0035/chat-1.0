package es.ubu.lsi.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import es.ubu.lsi.common.ChatMessage;

public class ChatClientImpl implements ChatClient {

	private String server;
	private String username;
	private int port;
	private boolean carryOn = true;
	private int id;

	public ChatClientImpl(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.carryOn = true;
		this.start();
	}

	public boolean start() {

		//TODO arrancar hilo cliente:
		//		Recorre mensajes server
		//		Excepcion --> carryOn = false
		
		try (Socket socket = new Socket(server, port);
				//outputStream = new DataOutputStream(socket.getOutputStream());
				//inputStream  = new DataInputStream(socket.getInputStream());
				//outputStream.writeObject(username);
				//outputStream.flush();
				//Id = inputStream.readInt();
				PrintStream output = new PrintStream(socket.getOutputStream());
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {
			while (stdIn.readLine() != null) {
				System.out.println("Echo: " + in.read());
			}
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + server);
			carryOn = false;
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + server);
			carryOn = false;
			System.exit(1);
		}

		return true;
	}

	public void sendMessage(ChatMessage msg) {
		// TODO
	}

	public void disconnect() {
		carryOn = false;
	}

	public static void main(String[] args) {

		if (args.length != 2) {
			System.err.println("Usage: java ChatClientImpl <address> <nick name>");
			System.exit(1);
		}

		new ChatClientImpl(args[0], 1500, args[1]);
		
		//while (carryOn = true){
			//Input teclado
		//}

	}

}

class ChatClientListener implements Runnable {

	public void run() {
		// TODO Auto-generated method stub

	}

}
