package com.github.lhervier.domino.oauth.server.ex.authorize;

import com.github.lhervier.domino.oauth.server.ex.BaseAuthException;
import com.github.lhervier.domino.oauth.server.model.error.authorize.AccessDeniedError;

public class AuthAccessDeniedException extends BaseAuthException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 8016993714664739993L;

	/**
	 * Constructeur
	 */
	public AuthAccessDeniedException() {
		super(new AccessDeniedError());
	}

	/**
	 * Constructeur
	 * @param message
	 */
	public AuthAccessDeniedException(String message) {
		super(message, new AccessDeniedError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public AuthAccessDeniedException(String message, Throwable cause) {
		super(message, cause, new AccessDeniedError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public AuthAccessDeniedException(Throwable cause) {
		super(cause, new AccessDeniedError());
	}
}
