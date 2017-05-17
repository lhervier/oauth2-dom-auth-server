package com.github.lhervier.domino.oauth.library.server.ex.grant;

import com.github.lhervier.domino.oauth.library.server.ex.GrantException;
import com.github.lhervier.domino.oauth.library.server.model.error.grant.InvalidClientError;

public class InvalidClientException extends GrantException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 3494299160598990258L;

	/**
	 * Constructeur
	 */
	public InvalidClientException() {
		super(new InvalidClientError());
	}

	/**
	 * Constructeur
	 * @param message
	 */
	public InvalidClientException(String message) {
		super(message, new InvalidClientError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public InvalidClientException(String message, Throwable cause) {
		super(message, cause, new InvalidClientError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public InvalidClientException(Throwable cause) {
		super(cause, new InvalidClientError());
	}

}
