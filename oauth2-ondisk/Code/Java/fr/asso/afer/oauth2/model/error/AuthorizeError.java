package fr.asso.afer.oauth2.model.error;

import fr.asso.afer.oauth2.model.StateResponse;

/**
 * Bean pour décrire une erreur d'autorisation
 * @author Lionel HERVIER
 */
public abstract class AuthorizeError extends StateResponse {

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
