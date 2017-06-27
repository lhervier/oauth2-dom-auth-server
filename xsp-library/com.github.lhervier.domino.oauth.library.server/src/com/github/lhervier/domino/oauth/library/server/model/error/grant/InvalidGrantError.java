package com.github.lhervier.domino.oauth.library.server.model.error.grant;

import com.github.lhervier.domino.oauth.common.model.error.GrantError;

public class InvalidGrantError extends GrantError {

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
