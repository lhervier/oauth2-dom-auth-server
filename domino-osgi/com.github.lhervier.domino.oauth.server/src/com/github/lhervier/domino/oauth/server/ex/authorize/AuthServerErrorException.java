package com.github.lhervier.domino.oauth.server.ex.authorize;

import com.github.lhervier.domino.oauth.server.ex.BaseAuthException;
import com.github.lhervier.domino.oauth.server.model.error.authorize.ServerError;

public class AuthServerErrorException extends BaseAuthException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 3698177808182780992L;

	/**
	 * Constructeur
	 */
	public AuthServerErrorException() {
		super(new ServerError());
	}

	/**
	 * Constructeur
	 * @param message
	 */
	public AuthServerErrorException(String message) {
		super(message, new ServerError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public AuthServerErrorException(String message, Throwable cause) {
		super(message, cause, new ServerError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public AuthServerErrorException(Throwable cause) {
		super(cause, new ServerError());
	}
}
