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
	 */
	public ServerErrorException() {
		super(new ServerError());
	}

	/**
	 * Constructeur
	 * @param message
	 */
	public ServerErrorException(String message) {
		super(message, new ServerError());
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public ServerErrorException(String message, Throwable cause) {
		super(message, cause, new ServerError());
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public ServerErrorException(Throwable cause) {
		super(cause, new ServerError());
	}
}
