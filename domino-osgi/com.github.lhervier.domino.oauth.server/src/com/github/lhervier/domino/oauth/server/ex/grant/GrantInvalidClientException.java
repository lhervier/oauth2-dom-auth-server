package com.github.lhervier.domino.oauth.server.ex.grant;

import com.github.lhervier.domino.oauth.server.ex.BaseGrantException;
import com.github.lhervier.domino.oauth.server.model.error.grant.InvalidClientError;

public class GrantInvalidClientException extends BaseGrantException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 3494299160598990258L;

	/**
	 * Constructeur
	 * @param message
	 */
	public GrantInvalidClientException(String message) {
		super(message, new InvalidClientError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public GrantInvalidClientException(String message, Throwable cause) {
		super(message, cause, new InvalidClientError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public GrantInvalidClientException(Throwable cause) {
		super(cause, new InvalidClientError());
	}

}
