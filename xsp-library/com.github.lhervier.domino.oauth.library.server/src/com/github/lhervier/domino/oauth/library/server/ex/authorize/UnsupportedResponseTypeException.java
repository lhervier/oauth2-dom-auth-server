package com.github.lhervier.domino.oauth.library.server.ex.authorize;

import com.github.lhervier.domino.oauth.library.server.ex.AuthorizeException;
import com.github.lhervier.domino.oauth.library.server.model.error.authorize.UnsupportedResponseTypeError;

public class UnsupportedResponseTypeException extends AuthorizeException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -112334089500453291L;

	/**
	 * Constructeur
	 */
	public UnsupportedResponseTypeException() {
		super(new UnsupportedResponseTypeError());
	}

	/**
	 * Constructeur
	 * @param message
	 */
	public UnsupportedResponseTypeException(String message) {
		super(message, new UnsupportedResponseTypeError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public UnsupportedResponseTypeException(String message, Throwable cause) {
		super(message, cause, new UnsupportedResponseTypeError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public UnsupportedResponseTypeException(Throwable cause) {
		super(cause, new UnsupportedResponseTypeError());
	}
}