package com.github.lhervier.domino.oauth.library.server.ex.authorize;

import com.github.lhervier.domino.oauth.library.server.ex.AuthorizeException;
import com.github.lhervier.domino.oauth.library.server.model.error.authorize.InvalidRequestError;

/**
 * Exception levée quand on a une requête "authorize" incorrecte.
 * @author Lionel HERVIER
 */
public class InvalidRequestException extends AuthorizeException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 6995063301241640216L;

	/**
	 * Constructeur
	 */
	public InvalidRequestException() {
		super(new InvalidRequestError());
	}

	/**
	 * Constructeur
	 * @param message le message
	 */
	public InvalidRequestException(String message) {
		super(message, new InvalidRequestError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public InvalidRequestException(String message, Throwable cause) {
		super(message, cause, new InvalidRequestError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public InvalidRequestException(Throwable cause) {
		super(cause, new InvalidRequestError());
	}
}
