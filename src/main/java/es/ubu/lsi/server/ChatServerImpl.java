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
				ServerThreadForClient serv = new ServerThreadForClient(clientSocket);

				// TODO: Deberiamos hacr que no se puedan conectar dos con el
				// mismo nick

				clientesOnline.put(idCliente, serv);

				System.out.println("Clientes conectados: ");
				for (ServerThreadForClient client : clientesOnline.values()) {
					System.out.println("  > " + client.getNick());
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

	// TODO falta comentar
	public void broadcast(ChatMessage message) {

		String mensaje = message.getMessage();
		System.out.println("Broadcast --> " + mensaje);

		// TODO Falta que el que banea no reciba del baneado.
		for (ServerThreadForClient client : clientesOnline.values()) {

			List<Integer> baneados = baneos.get(client.id);
			System.out.println("Baneados de " + client.getNick() + ": " + baneados);

			if (baneados.contains(message.getId())) {

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

	// TODO falta comentar.
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
			baneos.put(idCliente, new ArrayList<Integer>());

			try {

				in = new ObjectInputStream(socket.getInputStream());
				out = new ObjectOutputStream(socket.getOutputStream());

				nick = (String) in.readObject();

				System.out.println("Conectado el usuario " + nick + " con id " + id);
				System.out.println(baneos);

			} catch (IOException e) {
				System.out.println("Error en el hilo servidor. IO.");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			id = idCliente++;

		}

		@Override
		public void run() {
			boolean online = true;
			while (online) {
				try {
					mensaje = (ChatMessage) in.readObject();
					int idBaneado;
					int idSender;
					// System.out.println(mensaje.getType().toString());

					switch (mensaje.getType()) {

					case BAN:
						idBaneado = comprobarSiExisteUsuario(mensaje.getMessage());
						idSender = mensaje.getId(); // TODO: Ver porque siempre
													// devuelve 0.
						if (idBaneado == -1) {

							System.out.println("El nick introducido no coincide con ningún cliente conectado.");

						} else {

							System.out.println("ID DEL BANEADOR: " + idSender);
							List<Integer> baneados = baneos.get(idSender);
							baneados.add(idBaneado);
							baneos.put(idSender, baneados);
							System.out
									.println("El usuario " + mensaje.getMessage() + " ha sido baneado correctamente.");
						}
						break;

					case UNBAN:
						idBaneado = comprobarSiExisteUsuario(mensaje.getMessage());
						idSender = mensaje.getId();
						if (idBaneado == -1) {

							System.out.println("El nick introducido no coincide con ningún cliente conectado.");

						} else {

							List<Integer> baneados = baneos.get(idSender);
							baneados.remove(idBaneado);
							baneos.put(idSender, baneados);
							System.out.println(
									"El usuario " + mensaje.getMessage() + " ha sido desbaneado correctamente.");
						}
						break;

					case LOGOUT:
						idSender = mensaje.getId();
						System.out.println("> Usuario desconectado: " + nick);
						remove(idSender); // TODO No lo elimina
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
					e.printStackTrace();
				}

			} // fin while
		}

		// TODO Comentar
		private int comprobarSiExisteUsuario(String nick) {
			for (Map.Entry<Integer, ServerThreadForClient> entryClient : clientesOnline.entrySet()) {
				if (entryClient.getValue().getNick().equals(nick)) {
					return entryClient.getKey();
				}
			}
			return -1;
		}

		// TODO Comentar
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
