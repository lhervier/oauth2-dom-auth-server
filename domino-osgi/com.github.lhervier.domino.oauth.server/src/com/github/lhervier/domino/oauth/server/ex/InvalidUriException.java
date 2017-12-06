package com.github.lhervier.domino.oauth.server.ex;

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
	 * @param message
	 */
	public InvalidUriException(String message) {
		super(message);
	}
}
