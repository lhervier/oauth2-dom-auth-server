package com.github.lhervier.domino.oauth.server.ex.authorize;

import com.github.lhervier.domino.oauth.server.ex.AuthorizeException;
import com.github.lhervier.domino.oauth.server.model.error.authorize.ServerError;

public class AuthorizeServerErrorException extends AuthorizeException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 3698177808182780992L;

	/**
	 * Constructeur
	 */
	public AuthorizeServerErrorException() {
		super(new ServerError());
	}

	/**
	 * Constructeur
	 * @param message
	 */
	public AuthorizeServerErrorException(String message) {
		super(message, new ServerError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public AuthorizeServerErrorException(String message, Throwable cause) {
		super(message, cause, new ServerError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public AuthorizeServerErrorException(Throwable cause) {
		super(cause, new ServerError());
	}
}
