package com.github.lhervier.domino.oauth.library.server.ex.grant;

import com.github.lhervier.domino.oauth.library.server.ex.GrantException;
import com.github.lhervier.domino.oauth.library.server.model.error.grant.InvalidScopeError;

public class InvalidScopeException extends GrantException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 3494299160598990258L;

	/**
	 * Constructeur
	 */
	public InvalidScopeException() {
		super(new InvalidScopeError());
	}

	/**
	 * Constructeur
	 * @param message
	 */
	public InvalidScopeException(String message) {
		super(message, new InvalidScopeError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public InvalidScopeException(String message, Throwable cause) {
		super(message, cause, new InvalidScopeError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public InvalidScopeException(Throwable cause) {
		super(cause, new InvalidScopeError());
	}

}
