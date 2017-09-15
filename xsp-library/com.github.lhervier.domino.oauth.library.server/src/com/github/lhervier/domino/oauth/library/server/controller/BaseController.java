package com.github.lhervier.domino.oauth.library.server.controller;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.lhervier.domino.oauth.library.server.ex.InvalidUriException;

/**
 * Base class for all controllers
 * @author Lionel HERVIER
 */
public class BaseController {
	
	/**
	 * The http servlet request
	 */
	@Autowired
	private HttpServletRequest request;
	
	/**
	 * Check the redirectUri
	 */
	protected String getRedirectUri() throws InvalidUriException {
		String redirectUri = this.request.getParameter("redirect_uri");
		if( StringUtils.isEmpty(redirectUri) )
			throw new InvalidUriException("No redirect_uri in query string.");
		try {
			URI uri = new URI(redirectUri);
			if( !uri.isAbsolute() )
				throw new InvalidUriException("Invalid redirect_uri. Must be absolute.");
		} catch (URISyntaxException e) {
			throw new InvalidUriException("Invalid redirect_uri. Syntax is invalid.");
		}
		return redirectUri;
	}

}
