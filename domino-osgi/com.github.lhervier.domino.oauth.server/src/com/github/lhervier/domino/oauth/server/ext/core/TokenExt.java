package com.github.lhervier.domino.oauth.server.ext.core;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.ext.IAuthorizer;
import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.services.TimeService;

/**
 * Extension to manage "response_type=token" requests
 * @author Lionel HERVIER
 */
@Component(TokenExt.TOKEN_RESPONSE_TYPE)
public class TokenExt implements IOAuthExtension {

	public static final String TOKEN_RESPONSE_TYPE = "token";
	
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
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#getAuthorizedScopes()
	 */
	@Override
	public List<String> getAuthorizedScopes() {
		return new ArrayList<String>();
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#authorize(com.github.lhervier.domino.oauth.server.NotesPrincipal, com.github.lhervier.domino.oauth.server.model.Application, java.util.List, com.github.lhervier.domino.oauth.server.ext.IAuthorizer)
	 */
	@Override
	public void authorize(
			NotesPrincipal user,
			Application app,
			List<String> askedScopes,
			IAuthorizer authorizer) {
		AccessToken accessToken = new AccessToken();
		accessToken.setAud(app.getClientId());
		accessToken.setIss(this.iss);
		accessToken.setSub(user.getName());
		accessToken.setExp(this.timeSvc.currentTimeSeconds() + this.expiresIn);
		authorizer.addSignedProperty("access_token", accessToken, this.signKey);
		authorizer.addProperty("token_type", "bearer");
		authorizer.setContext(accessToken);
		
		authorizer.saveAuthCode(false);
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#token(com.github.lhervier.domino.oauth.server.NotesPrincipal, com.github.lhervier.domino.oauth.server.model.Application, java.lang.Object, java.util.List, com.github.lhervier.domino.oauth.server.ext.IPropertyAdder)
	 */
	@Override
	public void token(
			NotesPrincipal user, 
			Application app, 
			Object context, 
			List<String> askedScopes,
			IPropertyAdder adder) {
		throw new RuntimeException("TokenExt: Token endpoint not available");
	}
}
