package es.ubu.lsi.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.ubu.lsi.common.ChatMessage;

public class ChatServerImpl implements ChatServer {

	private static int DEFAULT_PORT = 1500;
	public SimpleDateFormat sdf;

	// Puerto por el que escucha el servidor.
	private int port;

	// Si el servidor esta arrancado.
	protected boolean alive;

	// Socket del servidor
	protected ServerSocket serverSocket;

	// Lista de los clientes conectados
	private Map<Integer, ServerThreadForClient> clientesOnline;

	// Mapa de los clientes y sus baneados. Clave: id. Valores: lista de ids.
	private Map<Integer, List<Integer>> baneos = new HashMap<Integer, List<Integer>>();

	public ChatServerImpl() {
		this.port = DEFAULT_PORT;
		this.alive = true;
		this.clientesOnline = new HashMap<Integer, ServerThreadForClient>();
	}

	public void startup() {

		alive = true;

		try {
			serverSocket = new ServerSocket(port);

			System.out.println("Esperando en el puerto " + port + "...");

			while (alive) {
				Socket clientSocket = serverSocket.accept();
				ServerThreadForClient clientThread = new ServerThreadForClient(clientSocket);

				// Si ya existe un mismo id en la lista d clientes, salta
				// excepción.
				if (clientesOnline.containsKey(clientThread.id))
					throw new Exception();

				clientesOnline.put(clientThread.id, clientThread);

				System.out.println("Clientes conectados: ");
				for (ServerThreadForClient client : clientesOnline.values()) {
					System.out.println("  > " + client.getNick());
				}
				clientThread.start();
			}
		} catch (IOException ioe) {
			System.out.println(
					"Exception caught when trying to listen on port " + port + " or listening for a connection");
		} catch (Exception e) {
			System.err.println("Se ha intentado crear un cliente con un nombre ya existente");
		}

		try {
			serverSocket.close();
		} catch (IOException e) {
			System.err.println("Error en el cierre del socket.");
			e.printStackTrace();
		}

	}

	public void shutdown() {
		// No hacer
		// alive = false;
	}

	public void broadcast(ChatMessage message) {

		String mensaje = message.getMessage();
		int idSender = message.getId();
		// System.out.println("Broadcast --> " + mensaje);

		for (ServerThreadForClient client : clientesOnline.values()) {

			List<Integer> baneados = baneos.get(client.id);

			if (baneados.contains(idSender)) {
				System.out.println("Está baneado, no mandamos nada...");
			} else {
				try {
					client.out.writeObject(mensaje);
				} catch (IOException e) {
					System.err.println("Error de broadcast.");
					e.printStackTrace();
				}
			}
		}

	}

	public void remove(int id) {
		clientesOnline.remove(id);// Ver por que no elimina del hashmap.
	}

	public Map<Integer, ServerThreadForClient> getClientesOnline() {
		return clientesOnline;
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

			try {
				in = new ObjectInputStream(socket.getInputStream());
				out = new ObjectOutputStream(socket.getOutputStream());

				mensaje = (ChatMessage) in.readObject();
				nick = mensaje.getMessage();
				id = mensaje.getId();

				// System.out.println("Conectado el usuario " + nick + " con id
				// " + id);

			} catch (IOException e) {
				System.out.println("Error en el hilo servidor. IO.");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			baneos.put(id, new ArrayList<Integer>());

		}

		@Override
		public void run() {
			boolean online = true;
			while (online) {
				try {
					mensaje = (ChatMessage) in.readObject();
					int idBaneado = comprobarSiExisteUsuario(mensaje.getMessage());
					int idSender = mensaje.getId();

					switch (mensaje.getType()) {

					case BAN:
						if (idBaneado == -1) {

							System.out.println("El nick introducido no coincide con ningún cliente conectado.");

						} else {

							List<Integer> baneados = baneos.get(idSender);
							baneados.add(idBaneado);
							baneos.put(idSender, baneados);
							System.out.println(
									"El usuario " + nick + " ha baneado correctamente a " + mensaje.getMessage());
						}
						break;

					case UNBAN:
						if (idBaneado == -1) {

							System.out.println("El nick introducido no coincide con ningún cliente conectado.");

						} else {

							List<Integer> baneados = baneos.get(idSender);
							baneados.remove(idBaneado);
							baneos.put(idSender, baneados);
							System.out.println(
									"El usuario " + nick + " ha desbaneado correctamente a " + mensaje.getMessage());
						}
						break;

					case LOGOUT:
						System.out.println("> Usuario desconectado: " + nick);
						remove(idSender);
						online = false;
						break;

					case MESSAGE:
						mensaje.setMessage("> " + nick + ": " + mensaje.getMessage());
						broadcast(mensaje);
						break;

					default:
						break;
					}

				} catch (Exception e) {
					System.err.println("El usuario " + nick + " se ha desconectado de forma inesperada.");
					remove(mensaje.getId());
					online = false;
				}

			} // fin while
		}

		private int comprobarSiExisteUsuario(String nick) {
			for (Map.Entry<Integer, ServerThreadForClient> entryClient : clientesOnline.entrySet()) {
				ServerThreadForClient cliente = entryClient.getValue();
				if (cliente.getNick().equals(nick)) {
					return entryClient.getKey();
				}
			}
			return -1;
		}

		public String getNick() {
			return nick;
		}

	}

	public static void main(String[] args) {
		if (args.length != 0) {
			System.err.println("Warning: No se necesitan parámetros.");
		}

		// Creamos un nuevo objeto servidor
		ChatServerImpl server = new ChatServerImpl();
		// Arrancamos el servidor.
		server.startup();
	}
}
