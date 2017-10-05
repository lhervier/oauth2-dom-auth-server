package com.github.lhervier.domino.oauth.server.model.error.authorize;


public class UnsupportedResponseTypeError extends AuthorizeError {

	/**
	 * Constructeur
	 */
	public UnsupportedResponseTypeError() {
		this.setError("unsupported_response_type");
		this.setErrorDescription(
				"The authorization server does not support obtaining an " +
				"authorization code using this method."
		);
		this.setErrorUri("http://lhervier.github.com/dom-auth-server/error/authorize/unsupported_response_type");
	}
}
