package fr.asso.afer.oauth2.ex.authorize;

import fr.asso.afer.oauth2.ex.AuthorizeException;
import fr.asso.afer.oauth2.model.error.authorize.AccessDeniedError;

public class AccessDeniedException extends AuthorizeException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 8016993714664739993L;

	/**
	 * Constructeur
	 * @param state
	 */
	public AccessDeniedException(String state) {
		super(new AccessDeniedError(state));
	}

	/**
	 * Constructeur
	 * @param message
	 * @param state
	 */
	public AccessDeniedException(String message, String state) {
		super(message, new AccessDeniedError(state));
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 * @param state
	 */
	public AccessDeniedException(String message, Throwable cause, String state) {
		super(message, cause, new AccessDeniedError(state));
	}

	/**
	 * Constructeur
	 * @param cause
	 * @param state
	 */
	public AccessDeniedException(Throwable cause, String state) {
		super(cause, new AccessDeniedError(state));
	}

	
}
