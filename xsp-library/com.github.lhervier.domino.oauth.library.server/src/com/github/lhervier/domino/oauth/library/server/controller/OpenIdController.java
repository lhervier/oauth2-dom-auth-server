package com.github.lhervier.domino.oauth.library.server.controller;

import lotus.domino.NotesException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.lhervier.domino.oauth.common.utils.ValueHolder;
import com.github.lhervier.domino.oauth.library.server.BearerSession;
import com.github.lhervier.domino.oauth.library.server.aop.ann.ServerRootContext;
import com.github.lhervier.domino.oauth.library.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.library.server.ext.IScopeGranter;
import com.github.lhervier.domino.oauth.library.server.ext.openid.IdToken;
import com.github.lhervier.domino.oauth.library.server.ext.openid.OpenIDExt;
import com.github.lhervier.domino.oauth.library.server.ext.openid.OpenIdContext;

/**
 * Controller to manage userInfo openId end point
 * @author Lionel HERVIER
 */
@Controller
public class OpenIdController {

	/**
	 * The bearer session
	 */
	@Autowired
	private BearerSession bearerSession;
	
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
	public @ResponseBody IdToken userInfo() throws NotesException {
		if( !this.bearerSession.isAvailable() )
			return null;
		
		// Run through authorize to get the context
		OpenIdContext ctx = this.openIdExt.authorize(
				this.bearerSession, 
				new IScopeGranter() {
					@Override
					public void grant(String scope) {
					}
				}, 
				this.bearerSession.getClientId(), 
				this.bearerSession.getScopes()
		);
		
		// Now, generate the token
		final ValueHolder<IdToken> idToken = new ValueHolder<IdToken>();
		this.openIdExt.token(
				ctx, 
				new IPropertyAdder() {
					@Override
					public void addCryptedProperty(String name, Object obj) {
					}
					@Override
					public void addSignedProperty(String name, Object obj) {
						if( "id_token".equals(name) )
							idToken.set((IdToken) obj);
					}
				}, 
				this.bearerSession.getScopes()
		);
		
		// Return the token
		return idToken.get();
	}
}
