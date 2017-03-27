package es.ubu.lsi.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import es.ubu.lsi.common.ChatMessage;

public class ChatServerImpl implements ChatServer {

	protected int idCliente = 0;
	
	private static int DEFAULT_PORT = 1500;
	public SimpleDateFormat sdf;

	// Puerto por el que escucha el servidor.
	private int port;

	// Si el servidor esta arrancado. *
	protected boolean alive;

	// Socket del servidor
	protected ServerSocket serverSocket;

	// Lista de los clientes conectados
	public List<ServerThreadForClient> clientesOnline;

	public ChatServerImpl() {
		this.port = DEFAULT_PORT;
		this.alive = true;
		this.clientesOnline = new ArrayList<ServerThreadForClient>();
	}

	public void startup() {

		alive = true;

		try {
			serverSocket = new ServerSocket(port);

			while (alive) {
				System.out.println("Esperando en el puerto " + port + "...");
				Socket clientSocket = serverSocket.accept();
				ServerThreadForClient serv = new ServerThreadForClient(clientSocket);
				clientesOnline.add(serv);
				// System.out.println("Cliente conectado!");
				System.out.println("Clientes conectados: ");
				for (ServerThreadForClient client : clientesOnline) {
					System.out.println("  > " + client.nick);
				}
				serv.start();
			}
		} catch (IOException e) {
			System.out.println(
					"Exception caught when trying to listen on port " + port + " or listening for a connection");
			System.out.println(e.getMessage());
		}

		try {
			serverSocket.close();
		} catch (IOException e) {
			System.err.println("Error en el cierre del socket.");
			e.printStackTrace();
		}

	}

	public void shutdown() {
		// TODO no hacer
		// alive = false;
	}

	public void broadcast(ChatMessage message) {

		String mensaje = message.getMessage();
		System.out.println("Broadcast --> " + mensaje);

		for (ServerThreadForClient client : clientesOnline) {
			try {
				client.out.writeObject(mensaje);
			} catch (IOException e) {
				System.err.println("Error de broadcast.");
				e.printStackTrace();
			}
		}

	}

	public void remove(int id) {
		// TODO elimina a un usuario de la lista
	}

	class ServerThreadForClient extends Thread {

		Socket socket;
		ObjectInputStream in;
		ObjectOutputStream out;

		private int id;
		private String nick;
		ChatMessage mensaje;

		/**
		 * Constructor
		 * 
		 * @param socketCliente
		 */
		public ServerThreadForClient(Socket socketCliente) {
			socket = socketCliente;
			id = idCliente++;
			try {
				in = new ObjectInputStream(socket.getInputStream());
				out = new ObjectOutputStream(socket.getOutputStream());
				nick = (String) in.readObject();
				System.out.println("Conectado el usuario " + nick + " con id " + id);
				// out.writeObject(id);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void run() {
			try {

				mensaje = (ChatMessage) in.readObject();
				System.out.println("El siguiente mensaje ha llegado al servidor: " + mensaje.getMessage());

			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			try {

				mensaje.setMessage(nick + ": " + mensaje.getMessage());
				broadcast(mensaje);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public static void main(String[] args) {
		if (args.length != 0) {
			System.err.println("Warning: No se necesitan parámetros.");
		}

		// Llamamos al constructor
		ChatServerImpl server = new ChatServerImpl();
		// Arranca el servidor.
		server.startup();
	}
}
