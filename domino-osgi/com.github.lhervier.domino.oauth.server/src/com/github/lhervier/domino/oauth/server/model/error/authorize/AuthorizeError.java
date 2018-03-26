package com.github.lhervier.domino.oauth.server.model.error.authorize;

import com.github.lhervier.domino.oauth.server.model.StateResponse;
import com.github.lhervier.domino.oauth.server.utils.QueryStringUtils.QueryStringName;

/**
 * Bean pour décrire une erreur d'autorisation
 * @author Lionel HERVIER
 */
public class AuthorizeError extends StateResponse {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -6887185022402436851L;

	/**
	 * L'erreur. Cf RFC OAUTH2 pour la liste possible des valeurs
	 */
	private String error;
	
	/**
	 * La description de l'erreur
	 */
	private String errorDescription;
	
	/**
	 * L'Uri de l'erreur
	 */
	private String errorUri;
	
	/**
	 * @return the error
	 */
	public String getError() {
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(String error) {
		this.error = error;
	}

	/**
	 * @return the errorDescription
	 */
	@QueryStringName("error_description")
	public String getErrorDescription() {
		return errorDescription;
	}

	/**
	 * @param errorDescription the errorDescription to set
	 */
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	/**
	 * @return the errorUri
	 */
	@QueryStringName("error_uri")
	public String getErrorUri() {
		return errorUri;
	}

	/**
	 * @param errorUri the errorUri to set
	 */
	public void setErrorUri(String errorUri) {
		this.errorUri = errorUri;
	}
}
