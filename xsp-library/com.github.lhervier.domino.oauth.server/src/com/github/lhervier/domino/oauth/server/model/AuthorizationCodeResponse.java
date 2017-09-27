package com.github.lhervier.domino.oauth.server.model;

import com.github.lhervier.domino.oauth.common.model.StateResponse;

/**
 * Response of the authorize endpoint when using the authorization code grant flow
 * @author Lionel HERVIER
 */
public class AuthorizationCodeResponse extends StateResponse {

	/**
	 * The authorization code
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
