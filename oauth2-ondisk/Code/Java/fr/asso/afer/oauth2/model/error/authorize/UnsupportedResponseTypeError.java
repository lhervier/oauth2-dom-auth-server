package fr.asso.afer.oauth2.model.error.authorize;

import fr.asso.afer.oauth2.Constants;
import fr.asso.afer.oauth2.model.error.AuthorizeError;

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
