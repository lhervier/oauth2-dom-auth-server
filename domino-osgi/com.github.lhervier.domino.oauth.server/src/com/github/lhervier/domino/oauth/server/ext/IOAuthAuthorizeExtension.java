package com.github.lhervier.domino.oauth.server.ext;

import java.util.List;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.model.Application;

/**
 * Extension that will only implement the authorize end point
 * @author Lionel HERVIER
 */
public interface IOAuthAuthorizeExtension {

	/**
	 * Return the list of authorized scopes.
	 */
	public List<String> getAuthorizedScopes();
	
	/**
	 * Authorize end point
	 * @param user the currently connected user
	 * @param app the app the user is authenticating to
	 * @param askedScopes scopes asked by the user
	 * @param authorizer object to authorize the request
	 */
	public void authorize(
			NotesPrincipal user,
			Application app,
			List<String> askedScopes,
			IAuthorizer authorizer);
	
	/**
	 * Grant end point
	 * @param user
	 * @param app
	 * @param context
	 * @param askedScopes
	 * @param adder
	 */
	public void token(
			NotesPrincipal user,
			Application app,
			Object context, 
			List<String> askedScopes,
			IPropertyAdder adder);
}
