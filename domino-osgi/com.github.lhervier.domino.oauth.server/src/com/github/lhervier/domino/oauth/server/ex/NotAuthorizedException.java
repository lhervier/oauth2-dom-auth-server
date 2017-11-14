package com.github.lhervier.domino.oauth.server.ex;

/**
 * Raised when a user does not have the right to access
 * an endpoint.
 * @author Lionel HERVIER
 */public class NotAuthorizedException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 5129382983444635767L;

	public NotAuthorizedException() {
		super("not_authorized");
	}
}
