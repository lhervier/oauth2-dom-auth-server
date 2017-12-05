package com.github.lhervier.domino.oauth.server.ex.grant;

import com.github.lhervier.domino.oauth.server.ex.BaseGrantException;
import com.github.lhervier.domino.oauth.server.model.error.grant.InvalidClientError;
import com.github.lhervier.domino.oauth.server.model.error.grant.ServerError;

public class GrantServerErrorException extends BaseGrantException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 3494299160598990258L;

	/**
	 * Constructeur
	 * @param message
	 */
	public GrantServerErrorException(String message) {
		super(message, new ServerError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public GrantServerErrorException(String message, Throwable cause) {
		super(message, cause, new ServerError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public GrantServerErrorException(Throwable cause) {
		super(cause, new InvalidClientError());
	}

}
