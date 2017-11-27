package com.github.lhervier.domino.oauth.server.ex.grant;

import com.github.lhervier.domino.oauth.server.ex.BaseGrantException;
import com.github.lhervier.domino.oauth.server.model.error.grant.InvalidRequestError;

public class GrantInvalidRequestException extends BaseGrantException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -3172178165973596140L;

	/**
	 * Constructeur
	 * @param message
	 */
	public GrantInvalidRequestException(String message) {
		super(message, new InvalidRequestError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public GrantInvalidRequestException(String message, Throwable cause) {
		super(message, cause, new InvalidRequestError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public GrantInvalidRequestException(Throwable cause) {
		super(cause, new InvalidRequestError());
	}
}
