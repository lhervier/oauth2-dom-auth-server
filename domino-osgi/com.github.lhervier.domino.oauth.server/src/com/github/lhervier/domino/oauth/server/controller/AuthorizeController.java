package com.github.lhervier.domino.oauth.server.controller;

import org.springframework.web.servlet.ModelAndView;

import com.github.lhervier.domino.oauth.server.ex.BaseAuthException;
import com.github.lhervier.domino.oauth.server.ex.ForbiddenException;
import com.github.lhervier.domino.oauth.server.ex.InvalidUriException;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.WrongPathException;

/**
 * Authorize endpoint
 * @author Lionel HERVIER
 */
public interface AuthorizeController {

	/**
	 * Authorize endpoint.
	 * Unable to inject user bean as a method argument...
	 */
	public ModelAndView authorize(
    		String responseType,
    		String clientId,
    		String scope,
    		String state,
    		String redirectUri) throws NotAuthorizedException, ForbiddenException, WrongPathException, BaseAuthException, InvalidUriException, ServerErrorException;
}
