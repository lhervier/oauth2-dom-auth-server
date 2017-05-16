package fr.asso.afer.oauth2.model.error.grant;

import fr.asso.afer.oauth2.Constants;
import fr.asso.afer.oauth2.model.error.GrantError;

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
		this.setErrorUri(Constants.NAMESPACE + "/error/grant/invalid_grant");
	}
}
