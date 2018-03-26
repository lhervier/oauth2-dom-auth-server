package com.github.lhervier.domino.oauth.server.services;

import java.util.Map;

import com.github.lhervier.domino.oauth.server.ex.BaseGrantException;
import com.github.lhervier.domino.oauth.server.model.Application;

/**
 * Grant service
 * @author Lionel HERVIER
 */
public interface GrantService {

	/**
	 * Generate the grant
	 */
	public Map<String, Object> createGrant(Application app) throws BaseGrantException;
}
