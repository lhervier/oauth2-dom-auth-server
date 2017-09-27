package com.github.lhervier.domino.oauth.server.ex.grant;

import com.github.lhervier.domino.oauth.server.ex.GrantException;
import com.github.lhervier.domino.oauth.server.model.error.grant.InvalidRequestError;

public class InvalidRequestException extends GrantException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -3172178165973596140L;

	/**
	 * Constructeur
	 */
	public InvalidRequestException() {
		super(new InvalidRequestError());
	}

	/**
	 * Constructeur
	 * @param message
	 */
	public InvalidRequestException(String message) {
		super(message, new InvalidRequestError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public InvalidRequestException(String message, Throwable cause) {
		super(message, cause, new InvalidRequestError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public InvalidRequestException(Throwable cause) {
		super(cause, new InvalidRequestError());
	}
}
