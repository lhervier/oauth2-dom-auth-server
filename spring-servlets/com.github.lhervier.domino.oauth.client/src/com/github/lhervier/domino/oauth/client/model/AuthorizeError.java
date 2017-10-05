package com.github.lhervier.domino.oauth.client.model;

import com.github.lhervier.domino.oauth.client.utils.QueryStringUtils.QueryStringName;

/**
 * Bean pour décrire une erreur d'autorisation
 * @author Lionel HERVIER
 */
public class AuthorizeError {

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
	 * The state
	 */
	private String state;
	
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

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}
}
