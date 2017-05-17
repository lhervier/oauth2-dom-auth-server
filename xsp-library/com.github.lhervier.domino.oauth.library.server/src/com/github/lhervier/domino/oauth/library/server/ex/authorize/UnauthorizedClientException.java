package com.github.lhervier.domino.oauth.library.server.ex.authorize;

import com.github.lhervier.domino.oauth.library.server.ex.AuthorizeException;
import com.github.lhervier.domino.oauth.library.server.model.error.authorize.UnauthorizedClientError;

public class UnauthorizedClientException extends AuthorizeException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -1959919623322631668L;

	/**
	 * Constructeur
	 */
	public UnauthorizedClientException() {
		super(new UnauthorizedClientError());
	}

	/**
	 * Constructeur
	 * @param message
	 */
	public UnauthorizedClientException(String message) {
		super(message, new UnauthorizedClientError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public UnauthorizedClientException(String message, Throwable cause) {
		super(message, cause, new UnauthorizedClientError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public UnauthorizedClientException(Throwable cause) {
		super(cause, new UnauthorizedClientError());
	}
}
