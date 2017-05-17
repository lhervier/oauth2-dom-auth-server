package com.github.lhervier.domino.oauth.library.server.ex;

import com.github.lhervier.domino.oauth.library.server.model.error.GrantError;

public class GrantException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -8148974226675543171L;

	/**
	 * L'erreur
	 */
	private GrantError error;
	
	/**
	 * Constructeur
	 * @param error l'erreur
	 */
	public GrantException(GrantError error) {
		super();
		this.error = error;
	}

	/**
	 * Constructeur
	 * @param message
	 * @param error l'erreur
	 */
	public GrantException(String message, GrantError error) {
		super(message);
		this.error = error;
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 * @param error l'erreur
	 */
	public GrantException(String message, Throwable cause, GrantError error) {
		super(message, cause);
		this.error = error;
	}

	/**
	 * Constructeur
	 * @param cause
	 * @param error l'erreur
	 */
	public GrantException(Throwable cause, GrantError error) {
		super(cause);
		this.error = error;
	}

	/**
	 * @return the error
	 */
	public GrantError getError() {
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(GrantError error) {
		this.error = error;
	}

}
