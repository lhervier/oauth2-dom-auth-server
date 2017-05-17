package com.github.lhervier.domino.oauth.library.server.model.error.authorize;

import com.github.lhervier.domino.oauth.library.server.Constants;
import com.github.lhervier.domino.oauth.library.server.model.error.AuthorizeError;

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
		this.setErrorUri(Constants.NAMESPACE + "/error/authorize/invalid_request");
	}
}
