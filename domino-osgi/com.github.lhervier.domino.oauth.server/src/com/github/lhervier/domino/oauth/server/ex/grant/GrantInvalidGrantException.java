package com.github.lhervier.domino.oauth.server.ex.grant;

import com.github.lhervier.domino.oauth.server.ex.BaseGrantException;
import com.github.lhervier.domino.oauth.server.model.error.grant.InvalidGrantError;

public class GrantInvalidGrantException extends BaseGrantException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -2418739712196438241L;

	/**
	 * Constructeur
	 * @param message
	 */
	public GrantInvalidGrantException(String message) {
		super(message, new InvalidGrantError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public GrantInvalidGrantException(String message, Throwable cause) {
		super(message, cause, new InvalidGrantError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public GrantInvalidGrantException(Throwable cause) {
		super(cause, new InvalidGrantError());
	}

}
