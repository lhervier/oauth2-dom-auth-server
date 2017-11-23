package com.github.lhervier.domino.oauth.server.services;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.ex.BaseAuthException;
import com.github.lhervier.domino.oauth.server.ex.InvalidUriException;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;

/**
 * Authorize endpoint
 * @author Lionel HERVIER
 */
public interface AuthorizeService {

	/**
	 * Authorize endpoint.
	 * @return the redirect url.
	 * @throws BaseAuthException If an error occur
	 * @throws InvalidUriException if the uri is invalid
	 * @throws ServerErrorException 
	 */
	public String authorize(
			NotesPrincipal user,
    		String responseType,
    		String clientId,
    		String scope,
    		String state,
    		String redirectUri) throws BaseAuthException, InvalidUriException, ServerErrorException;
}
