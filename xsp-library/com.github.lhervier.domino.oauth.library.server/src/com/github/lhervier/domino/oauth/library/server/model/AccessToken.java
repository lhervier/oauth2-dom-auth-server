package com.github.lhervier.domino.oauth.library.server.model;

import com.google.gson.annotations.SerializedName;

/**
 * Le access token
 * @author Lionel HERVIER
 */
public class AccessToken extends Token {

	/**
	 * La date d'expiration
	 */
	@SerializedName("exp")
	private long accessExp;

	/**
	 * @return the accessExp
	 */
	public long getAccessExp() {
		return accessExp;
	}

	/**
	 * @param accessExp the accessExp to set
	 */
	public void setAccessExp(long accessExp) {
		this.accessExp = accessExp;
	}
}
