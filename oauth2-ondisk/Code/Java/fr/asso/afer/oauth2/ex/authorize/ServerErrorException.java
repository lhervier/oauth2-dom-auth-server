package fr.asso.afer.oauth2.ex.authorize;

import fr.asso.afer.oauth2.ex.AuthorizeException;
import fr.asso.afer.oauth2.model.error.authorize.ServerError;

public class ServerErrorException extends AuthorizeException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 3698177808182780992L;

	/**
	 * Constructeur
	 * @param state
	 */
	public ServerErrorException(String state) {
		super(new ServerError(state));
	}

	/**
	 * Constructeur
	 * @param message
	 * @param state
	 */
	public ServerErrorException(String message, String state) {
		super(message, new ServerError(state));
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 * @param state
	 */
	public ServerErrorException(String message, Throwable cause, String state) {
		super(message, cause, new ServerError(state));
	}

	/**
	 * Constructeur
	 * @param cause
	 * @param state
	 */
	public ServerErrorException(Throwable cause, String state) {
		super(cause, new ServerError(state));
	}

	
}
