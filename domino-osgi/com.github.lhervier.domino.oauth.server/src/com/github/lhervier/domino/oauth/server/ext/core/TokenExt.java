package com.github.lhervier.domino.oauth.server.ext.core;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.ext.IAuthorizer;
import com.github.lhervier.domino.oauth.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.server.model.Application;

/**
 * Extension to manage "response_type=token" requests
 * @author Lionel HERVIER
 */
@Component(TokenExt.TOKEN_RESPONSE_TYPE)
public class TokenExt extends BaseCoreExt {

	public static final String TOKEN_RESPONSE_TYPE = "token";
	
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
			List<String> responseTypes,
			IAuthorizer authorizer) {
		AccessToken accessToken = this.createAccessToken(app, user);
		authorizer.addSignedProperty("access_token", accessToken, this.signKey);
		authorizer.addProperty("token_type", "bearer");
		
		authorizer.addCodeToResponse(false);
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
