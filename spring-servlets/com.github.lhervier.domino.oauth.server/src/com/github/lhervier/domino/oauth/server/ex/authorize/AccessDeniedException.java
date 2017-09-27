package com.github.lhervier.domino.oauth.server.ex.authorize;

import com.github.lhervier.domino.oauth.server.ex.AuthorizeException;
import com.github.lhervier.domino.oauth.server.model.error.authorize.AccessDeniedError;

public class AccessDeniedException extends AuthorizeException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 8016993714664739993L;

	/**
	 * Constructeur
	 */
	public AccessDeniedException() {
		super(new AccessDeniedError());
	}

	/**
	 * Constructeur
	 * @param message
	 */
	public AccessDeniedException(String message) {
		super(message, new AccessDeniedError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public AccessDeniedException(String message, Throwable cause) {
		super(message, cause, new AccessDeniedError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public AccessDeniedException(Throwable cause) {
		super(cause, new AccessDeniedError());
	}
}
