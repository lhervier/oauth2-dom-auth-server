package com.github.lhervier.domino.oauth.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
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
	 * @throws NotAuthorizedException
	 */
	public IdToken userInfo(NotesPrincipal user) throws NotAuthorizedException {
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
