package fr.asso.afer.oauth2.ex.grant;

import fr.asso.afer.oauth2.ex.GrantException;
import fr.asso.afer.oauth2.model.error.grant.InvalidRequestError;

public class InvalidRequestException extends GrantException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -3172178165973596140L;

	/**
	 * Constructeur
	 */
	public InvalidRequestException() {
		super(new InvalidRequestError());
	}

	/**
	 * Constructeur
	 * @param message
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
