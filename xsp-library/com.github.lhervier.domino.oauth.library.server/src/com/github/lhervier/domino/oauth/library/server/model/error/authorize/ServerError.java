package com.github.lhervier.domino.oauth.library.server.model.error.authorize;

import com.github.lhervier.domino.oauth.common.model.error.AuthorizeError;
import com.github.lhervier.domino.oauth.library.server.utils.Utils;

public class ServerError extends AuthorizeError {

	/**
	 * Constructeur
	 */
	public ServerError() {
		this.setError("server_error");
		this.setErrorDescription(
				"The authorization server encountered an unexpected " +
				"condition that prevented it from fulfilling the request. " +
				"(This error code is needed because a 500 Internal Server " +
				"Error HTTP status code cannot be returned to the client " +
				"via an HTTP redirect.)"
		);
		this.setErrorUri(Utils.getIssuer() + "/error/authorize/server_error");
	}
}
