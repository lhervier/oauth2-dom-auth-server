package com.github.lhervier.domino.oauth.server.ex.authorize;

import com.github.lhervier.domino.oauth.server.ex.BaseAuthException;
import com.github.lhervier.domino.oauth.server.model.error.authorize.UnsupportedResponseTypeError;

public class AuthUnsupportedResponseTypeException extends BaseAuthException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -112334089500453291L;

	/**
	 * Constructeur
	 */
	public AuthUnsupportedResponseTypeException() {
		super(new UnsupportedResponseTypeError());
	}

	/**
	 * Constructeur
	 * @param message
	 */
	public AuthUnsupportedResponseTypeException(String message) {
		super(message, new UnsupportedResponseTypeError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public AuthUnsupportedResponseTypeException(String message, Throwable cause) {
		super(message, cause, new UnsupportedResponseTypeError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public AuthUnsupportedResponseTypeException(Throwable cause) {
		super(cause, new UnsupportedResponseTypeError());
	}
}
