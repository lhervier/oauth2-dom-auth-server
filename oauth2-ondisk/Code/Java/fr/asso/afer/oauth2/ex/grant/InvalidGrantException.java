package fr.asso.afer.oauth2.ex.grant;

import fr.asso.afer.oauth2.ex.GrantException;
import fr.asso.afer.oauth2.model.error.grant.InvalidGrantError;

public class InvalidGrantException extends GrantException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -2418739712196438241L;

	/**
	 * Constructeur
	 */
	public InvalidGrantException() {
		super(new InvalidGrantError());
	}

	/**
	 * Constructeur
	 * @param message
	 */
	public InvalidGrantException(String message) {
		super(message, new InvalidGrantError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public InvalidGrantException(String message, Throwable cause) {
		super(message, cause, new InvalidGrantError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public InvalidGrantException(Throwable cause) {
		super(cause, new InvalidGrantError());
	}

}
