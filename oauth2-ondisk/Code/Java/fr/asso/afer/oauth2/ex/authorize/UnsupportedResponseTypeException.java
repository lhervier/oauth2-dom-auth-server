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
	 * @param state
	 */
	public UnsupportedResponseTypeException(String state) {
		super(new UnsupportedResponseTypeError(state));
	}

	/**
	 * Constructeur
	 * @param message
	 * @param state
	 */
	public UnsupportedResponseTypeException(String message, String state) {
		super(message, new UnsupportedResponseTypeError(state));
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 * @param state
	 */
	public UnsupportedResponseTypeException(String message, Throwable cause, String state) {
		super(message, cause, new UnsupportedResponseTypeError(state));
	}

	/**
	 * Constructeur
	 * @param cause
	 * @param state
	 */
	public UnsupportedResponseTypeException(Throwable cause, String state) {
		super(cause, new UnsupportedResponseTypeError(state));
	}

}
