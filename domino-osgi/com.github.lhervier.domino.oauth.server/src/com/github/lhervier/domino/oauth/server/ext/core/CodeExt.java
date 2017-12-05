package com.github.lhervier.domino.oauth.server.ext.core;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.ext.IAuthorizer;
import com.github.lhervier.domino.oauth.server.ext.IOAuthAuthorizeExtension;
import com.github.lhervier.domino.oauth.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.services.TimeService;

/**
 * Extension to manage "response_type=code" requests
 * @author Lionel HERVIER
 */
@Component(CodeExt.CODE_RESPONSE_TYPE)
public class CodeExt implements IOAuthAuthorizeExtension {

	public static final String CODE_RESPONSE_TYPE = "code";

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
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthAuthorizeExtension#getAuthorizedScopes()
	 */
	@Override
	public List<String> getAuthorizedScopes() {
		return new ArrayList<String>();
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthAuthorizeExtension#authorize(com.github.lhervier.domino.oauth.server.NotesPrincipal, com.github.lhervier.domino.oauth.server.model.Application, java.util.List, com.github.lhervier.domino.oauth.server.ext.IAuthorizer)
	 */
	@Override
	public void authorize(
			NotesPrincipal user,
			Application app,
			List<String> askedScopes,
			IAuthorizer authorizer) {
		// Create context object
		AccessToken accessToken = new AccessToken();
		accessToken.setAud(app.getClientId());
		accessToken.setIss(this.iss);
		accessToken.setSub(user.getName());
		accessToken.setExp(this.timeSvc.currentTimeSeconds() + this.expiresIn);
		authorizer.setContext(accessToken);
		
		authorizer.saveAuthCode(true);			// Explicitly ask to save the auth code
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthTokenExtension#token(java.lang.Object, com.github.lhervier.domino.oauth.server.ext.IPropertyAdder, com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity)
	 */
	public void token(
			NotesPrincipal user,
			Application app,
			Object context,
			List<String> askedScopes,
			IPropertyAdder adder) {
		AccessToken accessToken = (AccessToken) context;
		accessToken.setExp(this.timeSvc.currentTimeSeconds() + this.expiresIn);
		adder.addSignedProperty("access_token", accessToken, this.signKey);
		adder.addProperty("token_type", "bearer");
	}
}
