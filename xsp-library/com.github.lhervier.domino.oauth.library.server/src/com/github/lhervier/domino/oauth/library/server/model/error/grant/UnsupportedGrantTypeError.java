package com.github.lhervier.domino.oauth.library.server.model.error.grant;

import com.github.lhervier.domino.oauth.common.model.error.GrantError;
import com.github.lhervier.domino.oauth.library.server.Constants;

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
		this.setErrorUri(Constants.NAMESPACE + "/error/grant/unsupported_grant_type");
	}
}
