package fr.asso.afer.oauth2.ex.grant;

import fr.asso.afer.oauth2.ex.GrantException;
import fr.asso.afer.oauth2.model.error.grant.InvalidClientError;

public class InvalidClientException extends GrantException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 3494299160598990258L;

	/**
	 * Constructeur
	 */
	public InvalidClientException() {
		super(new InvalidClientError());
	}

	/**
	 * Constructeur
	 * @param message
	 */
	public InvalidClientException(String message) {
		super(message, new InvalidClientError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public InvalidClientException(String message, Throwable cause) {
		super(message, cause, new InvalidClientError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public InvalidClientException(Throwable cause) {
		super(cause, new InvalidClientError());
	}

}
