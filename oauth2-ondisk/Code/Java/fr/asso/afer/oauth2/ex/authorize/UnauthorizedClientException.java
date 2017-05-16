package fr.asso.afer.oauth2.ex.authorize;

import fr.asso.afer.oauth2.ex.AuthorizeException;
import fr.asso.afer.oauth2.model.error.authorize.UnauthorizedClientError;

public class UnauthorizedClientException extends AuthorizeException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -1959919623322631668L;

	/**
	 * Constructeur
	 * @param state
	 */
	public UnauthorizedClientException(String state) {
		super(new UnauthorizedClientError(state));
	}

	/**
	 * Constructeur
	 * @param message
	 * @param state
	 */
	public UnauthorizedClientException(String message, String state) {
		super(message, new UnauthorizedClientError(state));
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 * @param state
	 */
	public UnauthorizedClientException(String message, Throwable cause, String state) {
		super(message, cause, new UnauthorizedClientError(state));
	}

	/**
	 * Constructeur
	 * @param cause
	 * @param state
	 */
	public UnauthorizedClientException(Throwable cause, String state) {
		super(cause, new UnauthorizedClientError(state));
	}

}
