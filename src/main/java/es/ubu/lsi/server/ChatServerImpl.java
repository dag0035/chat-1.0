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

	public final int DEFAULT_PORT = 1500;
	public int clientId;

	public SimpleDateFormat sdf;
	/** Puerto por el que escucha el servidor. */
	public static int port;
	/** Si el servidor esta arrancado. */
	public boolean alive;

	public List<ServerThreadForClient> clientesOnline;

	public ChatServerImpl(int port) {
		this.port = port;
		this.alive = true;
		this.clientesOnline = new ArrayList<ServerThreadForClient>();

		startup();
	}

	public void startup() {
		try {
			while(alive){
				ServerSocket serverSocket = new ServerSocket(port);
	
				Socket clientSocket = serverSocket.accept();
	
				// TODO leer el nick de usuario.
	
				clientesOnline.add(new ServerThreadForClient(0, ""));
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
		byte[] buf = new byte[256];

		InetAddress group;
		try {
			group = InetAddress.getByName("230.0.0.1");
			DatagramPacket packet = new DatagramPacket(buf, buf.length, group, 4446);
			
			// socket.send(packet);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void remove(int id) {
		// TODO elimina a un usuario de la lista
	}

	
	public static void main(String[] args) {
		System.out.println("Arranca el main");
		
        int portName = Integer.parseInt(args[0]);
		
		// Arranca el servidor.
		if (args.length != 1) {
			System.err.println("Usage: java EchoServer <port number>");
			System.exit(1);
		}

		new ChatServerImpl(portName);
	}

}

class ServerThreadForClient extends Thread implements Runnable {
	private int id;
	private String nick;
	protected DatagramSocket socket = null;

	public ServerThreadForClient(int id, String nick) {
		this.id = id;
		this.nick = nick;

		try {
			socket = new DatagramSocket(4445);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (true) {
			try {
				byte[] buf = new byte[256];

				// receive request
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);

				// figure out response
				String dString = null;

				dString = "Hola";
				buf = dString.getBytes();

				InetAddress group = InetAddress.getByName("230.0.0.1");
				DatagramPacket packetEnv = new DatagramPacket(buf, buf.length, group, 4446);
				socket.send(packetEnv);

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
