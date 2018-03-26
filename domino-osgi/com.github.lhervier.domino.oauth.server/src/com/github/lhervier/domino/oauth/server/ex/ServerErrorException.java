package com.github.lhervier.domino.oauth.server.ex;

/**
 * Exception en cas d'erreur irratrapable sur le serveur
 * @author Lionel HERVIER
 */
public class ServerErrorException extends RuntimeException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 3698177808182780992L;
	
	/**
	 * Constructeur
	 * @param message the message
	 */
	public ServerErrorException(String message) {
		super(message);
	}
	
	/**
	 * Constructeur
	 * @param cause the cause
	 */
	public ServerErrorException(Exception cause) {
		super(cause);
	}
	
	/**
	 * Constructeur
	 * @param message the message
	 * @param cause the cause
	 */
	public ServerErrorException(String message, Exception cause) {
		super(message, cause);
	}
}
