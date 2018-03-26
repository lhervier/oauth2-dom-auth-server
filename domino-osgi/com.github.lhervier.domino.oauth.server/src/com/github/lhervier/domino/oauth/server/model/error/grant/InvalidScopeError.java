package com.github.lhervier.domino.oauth.server.model.error.grant;


public class InvalidScopeError extends GrantError {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -7456026187273995408L;

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
