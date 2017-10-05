package com.github.lhervier.domino.oauth.server.model.error.grant;


public class UnsupportedGrantTypeError extends GrantError {

	/**
	 * Constructeur
	 */
	public UnsupportedGrantTypeError() {
		this.setError("unsupported_grant_type");
		this.setErrorDescription(
				"The authorization grant type is not supported by the " +
				"authorization server."
		);
		this.setErrorUri("http://lhervier.github.com/dom-auth-server/error/grant/unsupported_grant_type");
	}
}
