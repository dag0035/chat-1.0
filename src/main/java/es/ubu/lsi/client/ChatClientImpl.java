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

			// input y output
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			System.out.println("Conectado al servidor como " + username);

			new Thread(new ChatClientListener()).start();

		} catch (IOException ioe) {
			System.err.println("No se puede establecer conexión con " + server + ":" + port);
			ioe.printStackTrace();
			carryOn = false;
		} catch (Exception e) {
			System.err.println("Error al obtener el socket");
			carryOn = false;
		}

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

	// TODO Comentar
	public void disconnect() {
		try {
			in.close();
			out.close();
			socket.close();

			carryOn = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Integer getId() {
		return id;
	}

	public class ChatClientListener implements Runnable {

		String mensaje;

		@Override
		public void run() {
			while (carryOn) {
				try {
					mensaje = (String) in.readObject();
					System.out.println(mensaje);
					System.out.print("> ");
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException ioe) {
					disconnect();// Nos aseguramos que en caso de que salte esta
									// excepción se desconecta.
					System.out.println(username + " te has desconectado.");
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

		// System.out.println("Usuario " + nick + " creado.");
		System.out.println("------------- CHAT -------------");

		cliente = new ChatClientImpl(server, 1500, nick);
		cliente.start();
		System.out.print("> ");

		// ---------------------------------------------------------//
		// Leer lo que escribimos y guardarlo en la variable mensaje
		while (online) {
			String mensaje = null;

			try {
				// Lee por teclado.
				BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
				mensaje = stdIn.readLine();

				String[] command = parseMensajeToCommand(mensaje);

				switch (command[0]) {
				case "BAN":
					// TODO Ban a un usuario suponiendo que el mensaje sea de
					// tipo ban, el mensaje será el usuario baneado.
					cliente.sendMessage(new ChatMessage(cliente.id, ChatMessage.MessageType.BAN, command[1]));
					break;
				case "LOGOUT":
					cliente.sendMessage(new ChatMessage(cliente.id, ChatMessage.MessageType.LOGOUT, ""));
					online = false;
					cliente.disconnect();
					break;
				default:
					cliente.sendMessage(new ChatMessage(cliente.id, ChatMessage.MessageType.MESSAGE, mensaje));
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
	}

	/**
	 * Método que lee el mensaje a mandar y selecciona la primera palabra como
	 * comando.
	 * 
	 * @param message
	 * @return String[] en el que el primer valor es el comando, y el segundo el
	 *         mesaje.
	 */
	private static String[] parseMensajeToCommand(String message) {
		return message.split(" ", 2);
	}

}
