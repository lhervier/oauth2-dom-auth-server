package com.github.lhervier.domino.oauth.server.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ext.IScopeGranter;
import com.github.lhervier.domino.oauth.server.ext.openid.IdToken;
import com.github.lhervier.domino.oauth.server.ext.openid.OpenIDExt;
import com.github.lhervier.domino.oauth.server.ext.openid.OpenIdContext;
import com.github.lhervier.domino.oauth.server.services.OpenIdUserInfoService;

/**
 * OpenID userInfo service
 * @author Lionel HERVIER
 */
@Service
public class OpenIdUserInfoServiceImpl implements OpenIdUserInfoService {

	/**
	 * The OpenId extension
	 */
	@Autowired
	private OpenIDExt openIdExt;
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.OpenIdUserInfoService#userInfo(com.github.lhervier.domino.oauth.server.NotesPrincipal)
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
