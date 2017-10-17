package com.github.lhervier.domino.oauth.server.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import lotus.domino.NotesException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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
	 * For CORS requests
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "/userInfo", method = RequestMethod.OPTIONS)
    @ResponseStatus(HttpStatus.OK)
	@ServerRootContext
	public void handleCors(HttpServletResponse response) throws IOException {
        response.addHeader("Access-Control-Allow-Headers", "authorization");
        response.addHeader("Access-Control-Max-Age", "60"); // seconds to cache preflight request --> less OPTIONS traffic
        response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.addHeader("Access-Control-Allow-Origin", "*");
    }
	
	/**
	 * The userInfo end point
	 * @return
	 * @throws NotesException 
	 */
	@RequestMapping(value = "/userInfo", method = RequestMethod.GET)
	@ServerRootContext
	public @ResponseBody IdToken userInfo(HttpServletResponse response) throws NotesException, NotAuthorizedException {
		response.addHeader("Access-Control-Allow-Origin", "*");
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
