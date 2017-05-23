package com.github.lhervier.domino.oauth.library.server.model;

import com.github.lhervier.domino.oauth.common.model.StateResponse;

/**
 * Réponse à l'appel du endpoint authorize
 * @author Lionel HERVIER
 */
public class AuthorizeResponse extends StateResponse {

	/**
	 * Le code autorisation
	 */
	private String code;

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}
}
