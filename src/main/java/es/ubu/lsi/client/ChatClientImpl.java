package es.ubu.lsi.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

	Socket socket = null;

	public ChatClientImpl(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;

		carryOn = true;
	}

	public boolean start() {

		try {
			// Conectar al servidor
			socket = new Socket(server, port);
		} catch (IOException ioe) {
			System.err.println("No se puede establecer conexión con " + server + ":" + port);
			ioe.printStackTrace();
			carryOn = false;
		}

		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			System.out.println("Conectado al servidor como " + username);
		} catch (Exception e) {
			System.err.println("Error al obtener el socket");
			carryOn = false;
		}

		new Thread(new ChatClientListener()).start();

		try {
			out.writeObject(username);
		} catch (IOException e) {
			System.out.println("Error al enviar username");
			carryOn = false;
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

		// TODO: Probar si funciona bien.
		
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
			while (carryOn) {
				try {
					mensaje = (String) in.readObject();
					System.out.println(mensaje);
					System.out.print("> "); // TODO: Arreglar el prompt para que escriba en la misma linea
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

		boolean online = true;

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
		
		//System.out.println("Usuario " + nick + " creado.");
		System.out.println("------------- CHAT -------------");

		cliente = new ChatClientImpl(server, 1500, nick);
		cliente.start();
		System.out.print("> "); // TODO: Arreglar el prompt para que escriba en la misma linea
		
		// ---------------------------------------------------------//
		// Leer lo que escribimos y guardarlo en la variable mensaje
		while (online) {
			// Poner online a False con la opcion LOGOUT

			// TODO: Hay que mirar si el mensaje empieza por BAN o LOGOUT para saber que tipo de mesaje es...
			String mensaje = null;
	
			
			try {
				BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
				mensaje = stdIn.readLine();

			} catch (Exception e) {
				// TODO: handle exception
			}
			// System.out.println("Prueba ----------> " + mensaje);
			cliente.sendMessage(new ChatMessage(cliente.id, ChatMessage.MessageType.MESSAGE, mensaje));

		}
	}

}
