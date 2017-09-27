package com.github.lhervier.domino.oauth.server.ex;

import com.github.lhervier.domino.oauth.common.model.error.AuthorizeError;

/**
 * Erreur pendant l'exécution du code grand 'authorize'
 * @author Lionel HERVIER
 */
public abstract class AuthorizeException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -2886333739068088636L;

	/**
	 * L'erreur associée
	 */
	private AuthorizeError error;
	
	/**
	 * Constructeur
	 * @param error l'erreur
	 */
	public AuthorizeException(AuthorizeError error) {
		super();
		this.error = error;
	}
	
	/**
	 * Constructeur
	 * @param message
	 */
	public AuthorizeException(String message, AuthorizeError error) {
		super(message);
		this.error = error;
	}
	
	/**
	 * Constructeur
	 * @param cause
	 */
	public AuthorizeException(Throwable cause, AuthorizeError error) {
		super(cause);
		this.error = error;
	}
	
	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public AuthorizeException(String message, Throwable cause, AuthorizeError error) {
		super(message, cause);
		this.error = error;
	}

	/**
	 * @return the error
	 */
	public AuthorizeError getError() {
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(AuthorizeError error) {
		this.error = error;
	}
}
