package com.github.lhervier.domino.oauth.library.server.ex.grant;

import com.github.lhervier.domino.oauth.library.server.ex.GrantException;
import com.github.lhervier.domino.oauth.library.server.model.error.grant.UnsupportedGrantTypeError;

public class UnsupportedGrantTypeException extends GrantException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 5770648849912500954L;

	/**
	 * Constructeur
	 */
	public UnsupportedGrantTypeException() {
		super(new UnsupportedGrantTypeError());
	}

	/**
	 * Constructeur
	 * @param message
	 */
	public UnsupportedGrantTypeException(String message) {
		super(message, new UnsupportedGrantTypeError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public UnsupportedGrantTypeException(String message, Throwable cause) {
		super(message, cause, new UnsupportedGrantTypeError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public UnsupportedGrantTypeException(Throwable cause) {
		super(cause, new UnsupportedGrantTypeError());
	}

}
