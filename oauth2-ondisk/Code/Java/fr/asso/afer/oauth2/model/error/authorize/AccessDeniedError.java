package fr.asso.afer.oauth2.model.error.authorize;

import fr.asso.afer.oauth2.Constants;
import fr.asso.afer.oauth2.model.error.AuthorizeError;

/**
 * Erreur d'autorisation
 * @author Lionel HERVIER
 */
public class AccessDeniedError extends AuthorizeError {

	/**
	 * Constructeur
	 */
	public AccessDeniedError() {
		this.setError("access_denied");
		this.setErrorDescription("The resource owner or authorization server denied the request.");
		this.setErrorUri(Constants.NAMESPACE + "/error/authorize/access_denied");
	}
}
