package com.github.lhervier.domino.oauth.server.ex;

/**
 * Exception en cas d'erreur irratrapable sur le serveur
 * @author Lionel HERVIER
 */
public class ServerErrorException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 3698177808182780992L;
	
	/**
	 * Constructeur
	 * @param message
	 */
	public ServerErrorException(String message) {
		super(message);
	}
}
