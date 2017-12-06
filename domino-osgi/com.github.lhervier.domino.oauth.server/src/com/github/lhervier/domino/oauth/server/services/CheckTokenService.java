package com.github.lhervier.domino.oauth.server.services;

import java.io.IOException;

import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.model.TokenContent;

/**
 * Service to check for tokens
 * @author Lionel HERVIER
 */
public interface CheckTokenService {

	/**
	 * Check if the token is OK
	 * @param userApp the current logged in user
	 * @param token the token to check
	 * @return the token
	 * @throws IOException
	 * @throws NotAuthorizedException
	 */
	public TokenContent checkToken(
			Application userApp, 
			String token) throws NotAuthorizedException;
}
