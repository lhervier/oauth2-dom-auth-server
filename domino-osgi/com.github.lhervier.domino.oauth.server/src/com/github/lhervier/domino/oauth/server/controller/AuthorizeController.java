package com.github.lhervier.domino.oauth.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.aop.ann.ctx.Oauth2DbContext;
import com.github.lhervier.domino.oauth.server.aop.ann.security.UserAuth;
import com.github.lhervier.domino.oauth.server.ex.BaseAuthException;
import com.github.lhervier.domino.oauth.server.ex.ForbiddenException;
import com.github.lhervier.domino.oauth.server.ex.InvalidUriException;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.WrongPathException;
import com.github.lhervier.domino.oauth.server.services.AuthorizeService;

/**
 * Authorize endpoint
 * @author Lionel HERVIER
 */
@Controller

@Oauth2DbContext			// Authorize endpoint is only accessible on the oauth2.nsf context
@UserAuth					// Authorize only possible if logged in a regular user
public class AuthorizeController {

	/**
	 * The authorize service
	 */
	@Autowired
	private AuthorizeService authSvc;
	
	/**
	 * The current user.
	 * We are unable to inject this into a method argument...
	 * (@Autowired properties are not instanciated)
	 */
	@Autowired
	private NotesPrincipal authorizeUser;
	
	/**
	 * Authorize endpoint.
	 * Unable to inject user bean as a method argument...
	 */
	@RequestMapping(value = "/authorize", method = RequestMethod.GET)
	public ModelAndView authorize(
    		@RequestParam(value = "response_type", required = false) String responseType,
    		@RequestParam(value = "client_id", required = false) String clientId,
    		@RequestParam(value = "scope", required = false) String scope,
    		@RequestParam(value = "state", required = false) String state,
    		@RequestParam(value = "redirect_uri", required = false) String redirectUri) throws NotAuthorizedException, ForbiddenException, WrongPathException, BaseAuthException, InvalidUriException, ServerErrorException {
		return new ModelAndView("redirect:" + this.authSvc.authorize(
				this.authorizeUser, 
				responseType, 
				clientId, 
				scope, 
				state, 
				redirectUri
		));
	}
}
