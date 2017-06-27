package com.github.lhervier.domino.oauth.library.server.model.error.authorize;

import com.github.lhervier.domino.oauth.common.model.error.AuthorizeError;

/**
 * Le client n'est pas autorisé à demander un code autorisation
 * @author Lionel HERVIER
 */
public class UnauthorizedClientError extends AuthorizeError {

	/**
	 * Constructeur
	 */
	public UnauthorizedClientError() {
		this.setError("unauthorized_client");
		this.setErrorDescription(
				"The client is not authorized to request an authorization " +
				"code using this method."
		);
		this.setErrorUri("http://lhervier.github.com/dom-auth-server/error/authorize/unauthorized_client");
	}
}
