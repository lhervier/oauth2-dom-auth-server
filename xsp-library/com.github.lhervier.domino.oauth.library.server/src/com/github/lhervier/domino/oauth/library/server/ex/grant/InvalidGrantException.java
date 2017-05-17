package com.github.lhervier.domino.oauth.library.server.ex.grant;

import com.github.lhervier.domino.oauth.library.server.ex.GrantException;
import com.github.lhervier.domino.oauth.library.server.model.error.grant.InvalidGrantError;

public class InvalidGrantException extends GrantException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -2418739712196438241L;

	/**
	 * Constructeur
	 */
	public InvalidGrantException() {
		super(new InvalidGrantError());
	}

	/**
	 * Constructeur
	 * @param message
	 */
	public InvalidGrantException(String message) {
		super(message, new InvalidGrantError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public InvalidGrantException(String message, Throwable cause) {
		super(message, cause, new InvalidGrantError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public InvalidGrantException(Throwable cause) {
		super(cause, new InvalidGrantError());
	}

}
