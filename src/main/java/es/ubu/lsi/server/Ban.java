package es.ubu.lsi.server;

public class Ban implements Comparable{
	private int idBaneador;
	private int idBaneado;
	
	public Ban(int idBaneador, int idBaneado){
		this.idBaneador = idBaneador;
		this.idBaneado = idBaneado;
	}

	@Override
	public int compareTo(Object ob) {	
		if(((Ban) ob).idBaneado == this.idBaneado && ((Ban) ob).idBaneador == this.idBaneador){
			return 0;
		} else if(((Ban) ob).idBaneador > this.idBaneador){
			return 1;
		} else {
			return -1;
		}
	}
	
}
