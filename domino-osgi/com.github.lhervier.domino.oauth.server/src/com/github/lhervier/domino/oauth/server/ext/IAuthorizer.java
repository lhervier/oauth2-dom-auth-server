package com.github.lhervier.domino.oauth.server.ext;

public interface IAuthorizer extends IPropertyAdder {

	/**
	 * Define the context object
	 */
	public void setContext(Object context);
	
	/**
	 * Ask to save the auth code.
	 */
	public void saveAuthCode(boolean save);
}
