package com.github.lhervier.domino.oauth.server.model.error.grant;


public class InvalidGrantError extends GrantError {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -228179263071673460L;

	/**
	 * Constructeur
	 */
	public InvalidGrantError() {
		this.setError("invalid_grant");
		this.setErrorDescription(
				"The provided authorization grant (e.g., authorization " +
				"code, resource owner credentials) or refresh token is " +
				"invalid, expired, revoked, does not match the redirection " +
				"URI used in the authorization request, or was issued to " +
				"another client."
		);
		this.setErrorUri("http://lhervier.github.com/dom-auth-server/error/grant/invalid_grant");
	}
}
