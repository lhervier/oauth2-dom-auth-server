package com.github.lhervier.domino.oauth.server.ext;

/**
 * Pour autoriser un scope
 * @author Lionel HERVIER
 */
public interface IScopeGranter {

	/**
	 * Grant a scope
	 * @param scope the scope to grant
	 */
	public void grant(String scope);
}
