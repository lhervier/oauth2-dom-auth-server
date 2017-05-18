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
		super("not_authorized");
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public NotAuthorizedException(Throwable cause) {
		super("not_authorized", cause);
	}
}
