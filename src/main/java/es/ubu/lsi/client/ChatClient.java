package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

/**
 * Interfaz para los clientes del chat.
 * 
 * @author Diego Martín Pérez.
 * @author Daniel Arnaiz Gutiérrez.
 */
public interface ChatClient {
	/** Inicia la conexión con el servidor. */
	public boolean start();
	/** Envia mensaje al servidor. */
	public void sendMessage(ChatMessage mesg);
	/** Desconecta al usuario del servidor. */
	public void disconnect();
}
