package com.github.lhervier.domino.oauth.server.model.error.authorize;

import com.github.lhervier.domino.oauth.common.model.error.AuthorizeError;

/**
 * Requête invalide
 * @author Lionel HERVIER
 */
public class InvalidRequestError extends AuthorizeError {

	/**
	 * Constructeur
	 */
	public InvalidRequestError() {
		this.setError("invalid_request");
		this.setErrorDescription(
				"The request is missing a required parameter, includes an " +
				"invalid parameter value, includes a parameter more than " +
				"once, or is otherwise malformed"
		);
		this.setErrorUri("http://lhervier.github.com/dom-auth-server/error/authorize/invalid_request");
	}
}
