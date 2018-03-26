package com.github.lhervier.domino.oauth.server.model.error.grant;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonProperty;

public class GrantError implements Serializable {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -1911358217878713013L;

	/**
	 * L'erreur
	 */
	private String error;
	
	/**
	 * La description de l'erreur
	 */
	@JsonProperty("error_description")
	private String errorDescription;
	
	/**
	 * L'uri de l'erreur
	 */
	@JsonProperty("error_uri")
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
