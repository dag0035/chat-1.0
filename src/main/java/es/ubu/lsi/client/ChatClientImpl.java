package es.ubu.lsi.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import es.ubu.lsi.common.ChatMessage;

public class ChatClientImpl implements ChatClient {

	private String server;
	private String username;
	private int port;
	private boolean carryOn = true;
	private int id;
	
	ObjectInputStream in;
	ObjectOutputStream out;

	public ChatClientImpl(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;
		
		carryOn = true;
	}

	public boolean start() {

		// TODO arrancar hilo cliente:
		// Recorre mensajes server
		// Excepcion --> carryOn = false

		Socket socket = null;
		
		try{
			//Conectar al servidor
			socket = new Socket(server, port);
		} catch(IOException ioe){
			System.err.println("No se puede establecer conexión con " + server + ":" + port);
			ioe.printStackTrace();
		}
		
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			System.out.println("Conectado al servidor");
		} catch (Exception e) {
			System.err.println("Error al obtener el socket");
		}
		
		new Thread(new ChatClientListener()).start();

		try {
			//System.out.println(username);
			out.writeObject(username);
		} catch (IOException e) {
			System.out.println("Error al enviar username");
		}
		return true;
	}

	public void sendMessage(ChatMessage msg) {
		try {
			out.writeObject(msg);
		} catch (IOException ioe) {
			System.err.println("Error al enviar el mensaje");
		}
	}

	public void disconnect() {
		
		try {
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		carryOn = false;
	}

	
	public class ChatClientListener implements Runnable {

		String mensaje;
		
		@Override
		public void run() {
			
			//id = in.readObject();
			
			while (carryOn) {
				try {
					System.out.println(in);
					mensaje = (String) in.readObject();
					System.out.println(mensaje);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
	}
	
	
	public static void main(String[] args) {
		String server = "";
		String nick = "";
		ChatClientImpl cliente;
		
		if (args.length == 1) {
			server = "localhost";
			nick = args[0];
		} else if (args.length == 2) {
			server = args[0];
			nick = args[1];
		} else {
			System.err.println("Usage: java ChatClientImpl <address> <nick name>");
			System.err.println("Usage: java ChatClientImpl <nick name>");
			System.exit(1);
		}
		System.out.println("Usuario " + nick + " creado.");

		cliente = new ChatClientImpl(server, 1500, nick);
		cliente.start();
	}
	
}
