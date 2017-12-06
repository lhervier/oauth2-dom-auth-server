package com.github.lhervier.domino.oauth.server.ext;

public interface IAuthorizer extends IPropertyAdder {

	/**
	 * Define the context object
	 */
	public void setContext(Object context);
	
	/**
	 * Ask to add the auth code id in the response (in the "code" attribute").
	 */
	public void addCodeToResponse(boolean save);
}
