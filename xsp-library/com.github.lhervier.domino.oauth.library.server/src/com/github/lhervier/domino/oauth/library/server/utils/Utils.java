package com.github.lhervier.domino.oauth.library.server.utils;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;

import com.github.lhervier.domino.oauth.library.server.ex.InvalidUriException;

public class Utils {

	/**
	 * Check the redirectUri
	 */
	public static final void checkRedirectUri(String redirectUri) throws InvalidUriException {
		if( StringUtils.isEmpty(redirectUri) )
			throw new InvalidUriException("No redirect_uri in query string.");
		try {
			URI uri = new URI(redirectUri);
			if( !uri.isAbsolute() )
				throw new InvalidUriException("Invalid redirect_uri. Must be absolute.");
		} catch (URISyntaxException e) {
			throw new InvalidUriException("Invalid redirect_uri. Syntax is invalid.");
		}
	}
}
