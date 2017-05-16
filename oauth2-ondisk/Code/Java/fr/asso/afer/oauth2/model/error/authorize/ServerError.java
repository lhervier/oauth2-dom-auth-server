package fr.asso.afer.oauth2.model.error.authorize;

import fr.asso.afer.oauth2.Constants;
import fr.asso.afer.oauth2.model.error.AuthorizeError;

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
		this.setErrorUri(Constants.NAMESPACE + "/error/authorize/server_error");
	}
}
