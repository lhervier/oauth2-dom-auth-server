package com.github.lhervier.domino.oauth.server.controller;

import lotus.domino.NotesException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.lhervier.domino.oauth.server.BearerContext;
import com.github.lhervier.domino.oauth.server.aop.ann.ServerRootContext;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ext.IScopeGranter;
import com.github.lhervier.domino.oauth.server.ext.openid.IdToken;
import com.github.lhervier.domino.oauth.server.ext.openid.OpenIDExt;
import com.github.lhervier.domino.oauth.server.ext.openid.OpenIdContext;

/**
 * Controller to manage userInfo openId end point
 * @author Lionel HERVIER
 */
@Controller
public class OpenIdController {

	/**
	 * The context sent from the Authorization Bearer http header
	 */
	@Autowired
	private BearerContext bearerContext;
	
	/**
	 * The OpenId extension
	 */
	@Autowired
	private OpenIDExt openIdExt;
	
	/**
	 * The userInfo end point
	 * @return
	 * @throws NotesException 
	 */
	@RequestMapping(value = "/userInfo", method = RequestMethod.GET)
	@ServerRootContext
	public @ResponseBody IdToken userInfo() throws NotesException, NotAuthorizedException {
		if( this.bearerContext.getBearerSession() == null )
		 throw new NotAuthorizedException();
		
		// Run through authorize to get the context
		OpenIdContext ctx = this.openIdExt.initContext(
				this.bearerContext.getBearerSession(), 
				new IScopeGranter() {
					@Override
					public void grant(String scope) {
					}
				}, 
				this.bearerContext.getBearerClientId(), 
				this.bearerContext.getBearerScopes()
		);
		
		// Now, generate the token
		IdToken idToken = this.openIdExt.createIdToken(ctx, this.bearerContext.getBearerScopes());
		
		// Return the token
		return idToken;
	}
}
