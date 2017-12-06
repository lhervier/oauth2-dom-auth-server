package com.github.lhervier.domino.oauth.server.ext.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.services.TimeService;

public abstract class BaseCoreExt implements IOAuthExtension {

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
	public AccessToken createAccessToken(Application app, NotesPrincipal user) {
		AccessToken accessToken = new AccessToken();
		accessToken.setAud(app.getClientId());
		accessToken.setIss(this.iss);
		accessToken.setSub(user.getName());
		accessToken.setExp(this.timeSvc.currentTimeSeconds() + this.expiresIn);
		return accessToken;
	}
}
