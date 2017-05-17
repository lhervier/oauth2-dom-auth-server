package com.github.lhervier.domino.oauth.library.server.model;

import com.github.lhervier.domino.oauth.common.utils.JsonUtils.JsonName;

/**
 * Le token de rafraîchissement
 * @author Lionel HERVIER
 */
public class RefreshToken extends Token {

	/**
	 * La date d'expiration
	 */
	private long refreshExp;

	/**
	 * @return the refreshExp
	 */
	@JsonName("exp")
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
