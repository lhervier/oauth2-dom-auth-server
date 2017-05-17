package com.github.lhervier.domino.oauth.library.server.model.error;

import com.github.lhervier.domino.oauth.common.utils.JsonUtils.JsonName;

public class GrantError {

	/**
	 * L'erreur
	 */
	private String error;
	
	/**
	 * La description de l'erreur
	 */
	private String errorDescription;
	
	/**
	 * L'uri de l'erreur
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
	@JsonName("error_description")
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
	@JsonName("error_uri")
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
