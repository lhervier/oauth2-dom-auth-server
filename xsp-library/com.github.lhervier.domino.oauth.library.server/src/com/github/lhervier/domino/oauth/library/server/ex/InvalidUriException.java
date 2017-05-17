package com.github.lhervier.domino.oauth.library.server.ex;

/**
 * Erreur levée si l'URI de redirection est invalide
 * @author Lionel HERVIER
 */
public class InvalidUriException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -4667171054867544585L;
	
	/**
	 * Constructeur
	 */
	public InvalidUriException() {
		super();
	}

	/**
	 * Constructeur
	 * @param message
	 */
	public InvalidUriException(String message) {
		super(message);
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public InvalidUriException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public InvalidUriException(Throwable cause) {
		super(cause);
	}

}
