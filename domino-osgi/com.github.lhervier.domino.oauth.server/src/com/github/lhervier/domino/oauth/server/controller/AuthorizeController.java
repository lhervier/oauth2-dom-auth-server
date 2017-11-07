package com.github.lhervier.domino.oauth.server.controller;

import java.io.IOException;

import lotus.domino.NotesException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.aop.ann.ctx.Oauth2DbContext;
import com.github.lhervier.domino.oauth.server.ex.AuthorizeException;
import com.github.lhervier.domino.oauth.server.ex.InvalidUriException;
import com.github.lhervier.domino.oauth.server.services.AuthorizeService;

/**
 * Authorize endpoint
 * @author Lionel HERVIER
 */
@Controller
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
	 * @throws AuthorizeException If an error occur
	 * @throws InvalidUriException if the uri is invalid
	 * @throws NotesException may happend...
	 */
	@RequestMapping(value = "/authorize", method = RequestMethod.GET)
	@Oauth2DbContext
    public ModelAndView authorize(
    		@RequestParam(value = "response_type", required = false) String responseType,
    		@RequestParam(value = "client_id", required = false) String clientId,
    		@RequestParam(value = "scope", required = false) String scope,
    		@RequestParam(value = "state", required = false) String state,
    		@RequestParam(value = "redirect_uri", required = false) String redirectUri) throws AuthorizeException, InvalidUriException, NotesException, IOException {
		return new ModelAndView(this.authSvc.authorize(
				this.authorizeUser, 
				responseType, 
				clientId, 
				scope, 
				state, 
				redirectUri
		));
	}
}
