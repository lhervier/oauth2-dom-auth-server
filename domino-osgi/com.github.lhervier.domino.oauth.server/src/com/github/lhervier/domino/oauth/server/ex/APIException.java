package com.github.lhervier.domino.oauth.server.ex;

/**
 * Exception raided when calling API Controller
 * @author Lionel hervier
 */
public class APIException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -8558423293139432846L;

	/**
	 * Constructor
	 */
	public APIException() {
		super();
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public APIException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor
	 * @param message
	 */
	public APIException(String message) {
		super(message);
	}

	/**
	 * Constructor
	 * @param cause
	 */
	public APIException(Throwable cause) {
		super(cause);
	}

}
