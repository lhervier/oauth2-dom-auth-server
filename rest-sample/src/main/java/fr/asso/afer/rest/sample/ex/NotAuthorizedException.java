package fr.asso.afer.rest.sample.ex;

public class NotAuthorizedException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 5468673052592671998L;

	/**
	 * Constructeur
	 */
	public NotAuthorizedException() {
		super();
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public NotAuthorizedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructeur
	 * @param message
	 */
	public NotAuthorizedException(String message) {
		super(message);
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public NotAuthorizedException(Throwable cause) {
		super(cause);
	}
}
