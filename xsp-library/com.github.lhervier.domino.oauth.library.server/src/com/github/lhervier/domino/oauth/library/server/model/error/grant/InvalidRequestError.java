package com.github.lhervier.domino.oauth.library.server.model.error.grant;

import com.github.lhervier.domino.oauth.library.server.Constants;
import com.github.lhervier.domino.oauth.library.server.model.error.GrantError;

public class InvalidRequestError extends GrantError {

	/**
	 * Constructeur
	 */
	public InvalidRequestError() {
		this.setError("invalid_request");
		this.setErrorDescription(
				"The request is missing a required parameter, includes an " +
				"unsupported parameter value (other than grant type), " +
				"repeats a parameter, includes multiple credentials, " +
				"utilizes more than one mechanism for authenticating the " +
				"client, or is otherwise malformed."
		);
		this.setErrorUri(Constants.NAMESPACE + "/error/grant/invalid_request");
	}
}
