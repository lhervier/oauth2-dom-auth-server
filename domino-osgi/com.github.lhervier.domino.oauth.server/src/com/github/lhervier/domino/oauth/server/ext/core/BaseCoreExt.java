package com.github.lhervier.domino.oauth.server.ext.core;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.ext.OAuthExtension;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.services.TimeService;

public abstract class BaseCoreExt implements OAuthExtension {

	/**
	 * The time service
	 */
	@Autowired
	protected TimeService timeSvc;
	
	/**
	 * The issuer
	 */
	@Value("${oauth2.server.core.iss}")
	protected String iss;
	
	/**
	 * Token expiration
	 */
	@Value("${oauth2.server.core.expiresIn}")
	protected long expiresIn;
	
	/**
	 * The sign key
	 */
	@Value("${oauth2.server.core.signKey}")
	protected String signKey;
	
	/**
	 * Create an access token
	 */
	public AccessToken createAccessToken(Application app, NotesPrincipal user, List<String> grantedScopes) {
		AccessToken accessToken = new AccessToken();
		accessToken.setAud(app.getClientId());
		accessToken.setIss(this.iss);
		accessToken.setSub(user.getName());
		accessToken.setExpires(this.timeSvc.currentTimeSeconds() + this.expiresIn);
		accessToken.setScope(StringUtils.join(grantedScopes.iterator(), ' '));
		return accessToken;
	}
}
