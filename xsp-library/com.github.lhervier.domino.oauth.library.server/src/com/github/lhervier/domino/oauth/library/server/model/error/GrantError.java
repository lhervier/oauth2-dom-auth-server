package com.github.lhervier.domino.oauth.library.server.model.error;

import com.google.gson.annotations.SerializedName;

public class GrantError {

	/**
	 * L'erreur
	 */
	private String error;
	
	/**
	 * La description de l'erreur
	 */
	@SerializedName("error_description")
	private String errorDescription;
	
	/**
	 * L'uri de l'erreur
	 */
	@SerializedName("error_uri")
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
