package fr.asso.afer.oauth2.ex.authorize;

import fr.asso.afer.oauth2.ex.AuthorizeException;
import fr.asso.afer.oauth2.model.error.authorize.UnsupportedResponseTypeError;

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
