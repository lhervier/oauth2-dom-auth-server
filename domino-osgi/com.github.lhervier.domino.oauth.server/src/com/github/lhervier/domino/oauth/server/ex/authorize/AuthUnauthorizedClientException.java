package com.github.lhervier.domino.oauth.server.ex.authorize;

import com.github.lhervier.domino.oauth.server.ex.BaseAuthException;
import com.github.lhervier.domino.oauth.server.model.error.authorize.UnauthorizedClientError;

public class AuthUnauthorizedClientException extends BaseAuthException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -1959919623322631668L;

	/**
	 * Constructeur
	 */
	public AuthUnauthorizedClientException() {
		super(new UnauthorizedClientError());
	}

	/**
	 * Constructeur
	 * @param message
	 */
	public AuthUnauthorizedClientException(String message) {
		super(message, new UnauthorizedClientError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public AuthUnauthorizedClientException(String message, Throwable cause) {
		super(message, cause, new UnauthorizedClientError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public AuthUnauthorizedClientException(Throwable cause) {
		super(cause, new UnauthorizedClientError());
	}
}
