package com.github.lhervier.domino.oauth.server.model.error.authorize;


public class ServerError extends AuthorizeError {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -8007740778881734109L;

	/**
	 * Constructeur
	 */
	public ServerError() {
		this.setError("server_error");
		this.setErrorDescription(
				"The authorization server encountered an unexpected " +
				"condition that prevented it from fulfilling the request. " +
				"(This error code is needed because a 500 Internal Server " +
				"Error HTTP status code cannot be returned to the client " +
				"via an HTTP redirect.)"
		);
		this.setErrorUri("http://lhervier.github.com/dom-auth-server/error/authorize/server_error");
	}
}
