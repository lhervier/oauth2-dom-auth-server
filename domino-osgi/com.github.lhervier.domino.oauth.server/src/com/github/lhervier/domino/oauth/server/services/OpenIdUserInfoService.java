package com.github.lhervier.domino.oauth.server.services;

import lotus.domino.NotesException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.NotesUserPrincipal;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ext.IScopeGranter;
import com.github.lhervier.domino.oauth.server.ext.openid.IdToken;
import com.github.lhervier.domino.oauth.server.ext.openid.OpenIDExt;
import com.github.lhervier.domino.oauth.server.ext.openid.OpenIdContext;

/**
 * OpenID userInfo service
 * @author Lionel HERVIER
 */
@Service
public class OpenIdUserInfoService {

	/**
	 * The OpenId extension
	 */
	@Autowired
	private OpenIDExt openIdExt;
	
	/**
	 * Extract the IdToken
	 * @param user the currently connected user
	 * @return the id token
	 * @throws NotesException
	 * @throws NotAuthorizedException
	 */
	public IdToken userInfo(NotesUserPrincipal user) throws NotesException, NotAuthorizedException {
		// Only allow bearer authentication
		if( !user.isBearerAuth() )
			throw new NotAuthorizedException();
		
		// Run through authorize to get the context
		OpenIdContext ctx = this.openIdExt.initContext(
				user, 
				new IScopeGranter() {
					@Override
					public void grant(String scope) {
					}
				}, 
				user.getClientId(),
				user.getScopes()
		);
		
		// Now, generate the token
		return this.openIdExt.createIdToken(ctx, user.getScopes());
	}
}
