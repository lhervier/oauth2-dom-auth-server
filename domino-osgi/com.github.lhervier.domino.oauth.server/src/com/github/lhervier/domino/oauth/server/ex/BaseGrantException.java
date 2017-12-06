package com.github.lhervier.domino.oauth.server.ex;

import com.github.lhervier.domino.oauth.server.model.error.grant.GrantError;

public class BaseGrantException extends Exception {

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
	 * @param message
	 * @param error l'erreur
	 */
	public BaseGrantException(String message, GrantError error) {
		super(message);
		this.error = error;
		this.error.setErrorDescription(message);
	}

	/**
	 * @return the error
	 */
	public GrantError getError() {
		return error;
	}
}
