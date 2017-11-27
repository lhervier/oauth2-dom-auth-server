package com.github.lhervier.domino.oauth.server.ex.grant;

import com.github.lhervier.domino.oauth.server.ex.BaseGrantException;
import com.github.lhervier.domino.oauth.server.model.error.grant.InvalidScopeError;

public class GrantInvalidScopeException extends BaseGrantException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 3494299160598990258L;

	/**
	 * Constructeur
	 * @param message
	 */
	public GrantInvalidScopeException(String message) {
		super(message, new InvalidScopeError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public GrantInvalidScopeException(String message, Throwable cause) {
		super(message, cause, new InvalidScopeError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public GrantInvalidScopeException(Throwable cause) {
		super(cause, new InvalidScopeError());
	}

}
