package com.github.lhervier.domino.oauth.server.ext;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseResponse implements TokenResponse {
	
	Map<String, OAuthProperty> properties = new HashMap<String, OAuthProperty>();
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.TokenResponse#getProperties()
	 */
	@Override
	public Map<String, OAuthProperty> getProperties() {
		return this.properties;
	}
}
