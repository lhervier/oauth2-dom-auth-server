package fr.asso.afer.oauth2.model.error.authorize;

import fr.asso.afer.oauth2.Constants;
import fr.asso.afer.oauth2.model.error.AuthorizeError;

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
		this.setErrorUri(Constants.NAMESPACE + "/error/authorize/unauthorized_client");
	}
}
