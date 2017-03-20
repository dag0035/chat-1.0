package es.ubu.lsi.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import es.ubu.lsi.common.ChatMessage;

public class ChatServerImpl implements ChatServer {

	private static int DEFAULT_PORT = 1500;
	private int clientId = -1;

	public SimpleDateFormat sdf;
	/** Puerto por el que escucha el servidor. */
	public static int port;
	/** Si el servidor esta arrancado. */
	public boolean alive;
	protected ServerSocket serverSocket;

	public List<ServerThreadForClient> clientesOnline;

	public ChatServerImpl() {
		this.port = DEFAULT_PORT;
		this.alive = true;
		this.clientesOnline = new ArrayList<ServerThreadForClient>();
		
		// Arranca el servidor.
		startup();
	}

	public void startup() {
		try {
			serverSocket = new ServerSocket(port);
			
			while (alive) {
				System.out.println("Esperando...");
				Socket clientSocket = serverSocket.accept();
				System.out.println("Usuario intentando conectar...");
				
				// TODO leer nick, y validar
				System.out.println("Cliente conectado!");
				ServerThreadForClient serv = new ServerThreadForClient(clientId++, "hola", serverSocket);
				clientesOnline.add(serv);
				serv.start();
			}
		} catch (IOException e) {
			System.out.println(
					"Exception caught when trying to listen on port " + port + " or listening for a connection");
			System.out.println(e.getMessage());
		}
	}

	public void shutdown() {
		// TODO no hacer
	}

	public void broadcast(ChatMessage message) {
		try {
			Socket clientSocket = serverSocket.accept();
			
			// DatagramPacket packet = new DatagramPacket(buf, buf.length);
			// clientSocket.receive(packet);
			//
			// InetAddress address = packet.getAddress();
			// int port = packet.getPort();
			// packet = new DatagramPacket(buf, buf.length, address, port);
			// socket.send(packet);
			

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void remove(int id) {
		// TODO elimina a un usuario de la lista
	}

	public static void main(String[] args) {
		// Arranca el servidor.
		if (args.length != 0) {
			System.err.println("Warning: No se necesitan parámetros.");
		}
		
		new ChatServerImpl();
	}

}

class ServerThreadForClient extends Thread implements Runnable {
	private int id;
	private String nick;
	protected DatagramSocket socket = null;
	private ServerSocket serverSocket;

	public ServerThreadForClient(int id, String nick, ServerSocket servSock) {
		this.id = id;
		this.nick = nick;
		this.serverSocket = servSock;
	}

	public void run() {
		while (true) {
			try {
				System.out.println("Aqui llega y espera paciente");
				Socket clientSocket = serverSocket.accept();
				
				System.out.println("Crea socket");
				socket = new DatagramSocket(1501);
				System.out.println("Lo creó!");

				byte[] buf = new byte[256];
				System.out.println("aja");
				// receive request
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				System.out.println("clack");
				socket.receive(packet);

				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

				// figure out response
				// String dString = null;
				//
				// dString = "Hola";
				// buf = dString.getBytes();
				// System.out.println("llega a mitad");
				// InetAddress group = InetAddress.getByName("230.0.0.1");
				// DatagramPacket packetEnv = new DatagramPacket(buf,
				// buf.length, group, 1501);
				// socket.send(packetEnv);

				// send the response to the client at "address" and "port"
				// InetAddress address = packet.getAddress();
				// int port = packet.getPort();
				// packet = new DatagramPacket(buf, buf.length, address, port);
				// socket.send(packet);

			} catch (IOException e) {
				e.printStackTrace();
				// moreQuotes = false;
			}
			socket.close();
		}

	}

}
