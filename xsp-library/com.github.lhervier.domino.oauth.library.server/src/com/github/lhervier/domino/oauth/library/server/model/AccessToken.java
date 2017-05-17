package com.github.lhervier.domino.oauth.library.server.model;

import com.github.lhervier.domino.oauth.common.utils.JsonUtils.JsonName;

/**
 * Le access token
 * @author Lionel HERVIER
 */
public class AccessToken extends Token {

	/**
	 * La date d'expiration
	 */
	private long accessExp;

	/**
	 * @return the accessExp
	 */
	@JsonName("exp")
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
