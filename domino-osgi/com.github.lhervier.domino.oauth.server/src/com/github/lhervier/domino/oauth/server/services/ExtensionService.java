package com.github.lhervier.domino.oauth.server.services;

import java.util.List;

import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;

/**
 * Service to manage extensions (like openid)
 * @author Lionel HERVIER
 */
public interface ExtensionService {

	/**
	 * Returns the extensions
	 * @return the extensions
	 */
	public List<? extends IOAuthExtension<?>> getExtensions();
}
