package es.ubu.lsi.server;

import es.ubu.lsi.common.ChatMessage;

/**
 * Interfaz para el servidor del chat. 
 * 
 * @author Diego Martín Pérez.
 * @author Daniel Arnaiz Gutiérrez.
 */
public interface ChatServer {
	
	/** Inicia el servidor */
	public void startup();
	/** Apaga el servidor */
	public void shutdown();
	/** Hace broadcast de un mensaje a todos los usuarios.*/
	public void broadcast(ChatMessage message);
	/** 
	 * Elimina un usuario dado su id.
	 * @param id Id del usuario a eliminar.
	 */
	public void remove(int id);
	
}
