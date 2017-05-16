package fr.asso.afer.oauth2.ex.grant;

import fr.asso.afer.oauth2.ex.GrantException;
import fr.asso.afer.oauth2.model.error.grant.UnsupportedGrantTypeError;

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
