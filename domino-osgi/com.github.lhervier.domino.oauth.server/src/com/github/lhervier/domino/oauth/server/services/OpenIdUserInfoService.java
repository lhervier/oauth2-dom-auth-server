package com.github.lhervier.domino.oauth.server.services;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ext.openid.IdToken;

/**
 * OpenID userInfo service
 * @author Lionel HERVIER
 */
public interface OpenIdUserInfoService {

	/**
	 * Extract the IdToken
	 * @param user the currently connected user
	 * @return the id token
	 * @throws NotAuthorizedException
	 */
	public IdToken userInfo(NotesPrincipal user) throws NotAuthorizedException;
}
