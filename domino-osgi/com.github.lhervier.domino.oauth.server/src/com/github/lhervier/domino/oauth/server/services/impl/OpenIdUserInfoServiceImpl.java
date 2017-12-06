package com.github.lhervier.domino.oauth.server.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ext.openid.OpenIDExt;
import com.github.lhervier.domino.oauth.server.ext.openid.IdToken;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.services.AppService;
import com.github.lhervier.domino.oauth.server.services.OpenIdUserInfoService;

/**
 * OpenID userInfo service
 * @author Lionel HERVIER
 */
@Service
public class OpenIdUserInfoServiceImpl implements OpenIdUserInfoService {

	/**
	 * The app repository
	 */
	@Autowired
	private AppService appSvc;
	
	/**
	 * The OpenId extension
	 */
	@Autowired
	private OpenIDExt openIdExt;
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.OpenIdUserInfoService#userInfo(com.github.lhervier.domino.oauth.server.NotesPrincipal)
	 */
	public IdToken userInfo(NotesPrincipal user) throws NotAuthorizedException {
		Application app = this.appSvc.getApplicationFromClientId(user.getClientId());
		return this.openIdExt.createIdToken(user, app, user.getScopes(), null);
	}
}
