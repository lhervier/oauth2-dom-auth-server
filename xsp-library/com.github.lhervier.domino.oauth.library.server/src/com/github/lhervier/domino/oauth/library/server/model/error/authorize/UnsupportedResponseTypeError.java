package com.github.lhervier.domino.oauth.library.server.model.error.authorize;

import com.github.lhervier.domino.oauth.common.model.error.AuthorizeError;
import com.github.lhervier.domino.oauth.library.server.Constants;

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
		this.setErrorUri(Constants.NAMESPACE + "/error/authorize/unsupported_response_type");
	}
}
