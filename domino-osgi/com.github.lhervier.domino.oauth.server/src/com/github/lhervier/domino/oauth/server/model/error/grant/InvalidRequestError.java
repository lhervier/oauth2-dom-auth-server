package com.github.lhervier.domino.oauth.server.model.error.grant;


public class InvalidRequestError extends GrantError {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 4257759249634823251L;

	/**
	 * Constructeur
	 */
	public InvalidRequestError() {
		this.setError("invalid_request");
		this.setErrorDescription(
				"The request is missing a required parameter, includes an " +
				"unsupported parameter value (other than grant type), " +
				"repeats a parameter, includes multiple credentials, " +
				"utilizes more than one mechanism for authenticating the " +
				"client, or is otherwise malformed."
		);
		this.setErrorUri("http://lhervier.github.com/dom-auth-server/error/grant/invalid_request");
	}
}
