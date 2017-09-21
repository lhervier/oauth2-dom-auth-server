package com.github.lhervier.domino.oauth.library.server.ext.core;

import java.util.List;

/**
 * Representation of the access token
 * @author Lionel HERVIER
 */
public class AccessToken extends CoreContext {

	/**
	 * Expiration
	 */
	private long exp;
	
	/**
	 * The scopes
	 */
	private List<String> scopes;

	public List<String> getScopes() {
		return scopes;
	}

	public void setScopes(List<String> scopes) {
		this.scopes = scopes;
	}

	public long getExp() {
		return exp;
	}

	public void setExp(long exp) {
		this.exp = exp;
	}
}
