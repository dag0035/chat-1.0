package es.ubu.lsi.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import es.ubu.lsi.common.ChatMessage;
import es.ubu.lsi.common.ChatMessage.MessageType;

/**
 * Clase ChatClienteImpl que implementa la interfaz ChatClient.
 * 
 * @author Diego Martín Pérez
 * @author Daniel Arnaiz Gutiérrez
 *
 */
public class ChatClientImpl implements ChatClient {
	/** Servidor al que se conecta. */
	private String server;
	/** Nombre de usuario */
	private String username;
	/** Puerto en el que esta el servidor. */
	private int port;
	/** Si esta conectado al servidor. */
	private boolean carryOn = true;
	/** Id de cliente */
	private int idCliente;

	/** Objeto de recepción de datos del servidor. */
	private ObjectInputStream in;
	/** Objeto para enviar datos al servidor. */
	private ObjectOutputStream out;

	/** Soket en el cliente. */
	private Socket socket = null;

	/**
	 * Constructor del cliente. Se inicializan las variables necesarias.
	 * 
	 * @param server
	 *            Direccion del server al que se conecta.
	 * @param port
	 *            Del servidor al que se conecta.
	 * @param username
	 *            Nombre del usuario.
	 */
	public ChatClientImpl(String server, int port, String username) {
		this.idCliente = username.hashCode();
		this.server = server;
		this.port = port;
		this.username = username;

		carryOn = true;
	}

	/**
	 * Inicia la conexión con el servidor.
	 * 
	 * @return true o false en función de si esta o no conectado.
	 */
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
			ChatMessage message = new ChatMessage(idCliente, MessageType.MESSAGE, username);
			out.writeObject(message);

		} catch (IOException e) {
			System.out.println("Error al enviar username");
			carryOn = false;
		}

		return true;
	}

	/**
	 * Envia un mensaje al servidor.
	 * 
	 * @param msg
	 *            Mensaje que envia al servidor.
	 */
	public void sendMessage(ChatMessage msg) {
		try {
			out.writeObject(msg);
		} catch (IOException ioe) {
			System.err.println("Error al enviar el mensaje");
		}
	}

	/**
	 * El usuario se desconecta del servidor.
	 */
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

	/**
	 * InnerClass que crea un hilo a parte, para el control de lo que se recibe
	 * del servidor.
	 */
	public class ChatClientListener implements Runnable {
		/** Mensaje que se recibe desde el servidor. */
		private String mensaje;

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

	/**
	 * Main del cliente que lo ejecuta.
	 * 
	 * En el main, se ejecuta un bucle que se queda a la espera de recibir
	 * contenido por teclado, y despues lo manda al servidor.
	 * 
	 * @param args
	 *            Argumentos que se le pasan al cliente. - Si solo se le pasa un
	 *            argumento es el nick, y por defecto la dirección es localhost.
	 *            - Si se le pasan dos argumentos el primero es la dirección y
	 *            el segundo el nick.
	 */
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

		System.out.println("------------- CHAT -------------");
		cliente = new ChatClientImpl(server, 1500, nick);
		cliente.start();

		// ---------------------------------------------------------//
		// Leer lo que escribimos y guardarlo en la variable mensaje
		while (online) {
			String mensaje = null;

			System.out.print("> ");
			try {
				// Lee por teclado.
				BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
				mensaje = stdIn.readLine();

				String[] command = parseMensajeToCommand(mensaje);

				switch (command[0]) {
				case "ban":
					cliente.sendMessage(new ChatMessage(cliente.idCliente, ChatMessage.MessageType.BAN, command[1]));
					break;
				case "unban":
					cliente.sendMessage(new ChatMessage(cliente.idCliente, ChatMessage.MessageType.UNBAN, command[1]));
					break;
				case "logout":
					cliente.sendMessage(new ChatMessage(cliente.idCliente, ChatMessage.MessageType.LOGOUT, ""));
					online = false;
					cliente.disconnect();
					break;
				default:
					cliente.sendMessage(new ChatMessage(cliente.idCliente, ChatMessage.MessageType.MESSAGE, mensaje));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Método que lee el mensaje a mandar y selecciona la primera palabra como
	 * comando.
	 * 
	 * @param message Mensaje a parsear para comprobar si es un comando.
	 * @return String[] en el que el primer valor es el comando, y el segundo el
	 *         mensaje.
	 */
	private static String[] parseMensajeToCommand(String message) {
		return message.split(" ", 2);
	}

}
