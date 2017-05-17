package com.github.lhervier.domino.oauth.library.server.model;

import com.google.gson.annotations.SerializedName;

/**
 * Le token de rafraîchissement
 * @author Lionel HERVIER
 */
public class RefreshToken extends Token {

	/**
	 * La date d'expiration
	 */
	@SerializedName("exp")
	private long refreshExp;

	/**
	 * @return the refreshExp
	 */
	public long getRefreshExp() {
		return refreshExp;
	}

	/**
	 * @param refreshExp the refreshExp to set
	 */
	public void setRefreshExp(long refreshExp) {
		this.refreshExp = refreshExp;
	}
}
