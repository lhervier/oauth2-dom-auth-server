package com.github.lhervier.domino.oauth.server.services;

import java.util.List;

import com.github.lhervier.domino.oauth.server.ext.IOAuthAuthorizeExtension;

/**
 * Service to manage extensions (like openid)
 * @author Lionel HERVIER
 */
public interface ExtensionService {

	/**
	 * Return the list of supported response types
	 */
	public List<String> getResponseTypes();
	
	/**
	 * Return the extension for the given response type
	 */
	public IOAuthAuthorizeExtension getExtension(String responseType);
}
