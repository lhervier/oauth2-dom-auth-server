package com.github.lhervier.domino.oauth.server.services;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.ex.BaseAuthException;
import com.github.lhervier.domino.oauth.server.ex.InvalidUriException;
import com.github.lhervier.domino.oauth.server.model.AuthorizeRequest;

/**
 * Authorize endpoint
 * @author Lionel HERVIER
 */
public interface AuthorizeService {

	/**
	 * Authorize endpoint.
	 * @param user the current user
	 * @param authReq the request
	 * @return the redirect url.
	 * @throws BaseAuthException If an error occur
	 * @throws InvalidUriException if the uri is invalid
	 */
	public String authorize(
			NotesPrincipal user,
			AuthorizeRequest authReq) throws BaseAuthException, InvalidUriException;
}
