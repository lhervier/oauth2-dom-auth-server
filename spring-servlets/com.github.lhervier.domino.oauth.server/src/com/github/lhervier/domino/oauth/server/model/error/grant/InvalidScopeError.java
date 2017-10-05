package com.github.lhervier.domino.oauth.server.model.error.grant;


public class InvalidScopeError extends GrantError {

	/**
	 * Constructeur
	 */
	public InvalidScopeError() {
		this.setError("invalid_scope");
		this.setErrorDescription(
				"The requested scope is invalid, unknown, malformed, or " +
				"exceeds the scope granted by the resource owner."
		);
		this.setErrorUri("http://lhervier.github.com/dom-auth-server/error/grant/invalid_scope");
	}
}
