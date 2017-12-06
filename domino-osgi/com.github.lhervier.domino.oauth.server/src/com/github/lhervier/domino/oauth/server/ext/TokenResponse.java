package com.github.lhervier.domino.oauth.server.ext;

import java.util.Map;

public interface TokenResponse {

	public Map<String, OAuthProperty> getProperties();
}
