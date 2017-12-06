package com.github.lhervier.domino.oauth.server.ext.core;

import java.util.List;

import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.ext.AuthorizeResponse;
import com.github.lhervier.domino.oauth.server.ext.AuthorizeResponseBuilder;
import com.github.lhervier.domino.oauth.server.ext.TokenResponse;
import com.github.lhervier.domino.oauth.server.model.Application;

/**
 * Extension to manage "response_type=token" requests
 * @author Lionel HERVIER
 */
@Component(TokenExt.TOKEN_RESPONSE_TYPE)
public class TokenExt extends BaseCoreExt {

	public static final String TOKEN_RESPONSE_TYPE = "token";
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.OAuthExtension#getAuthorizedScopes(List)
	 */
	@Override
	public List<String> getAuthorizedScopes(List<String> scopes) {
		return null;
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.OAuthExtension#authorize(com.github.lhervier.domino.oauth.server.NotesPrincipal, com.github.lhervier.domino.oauth.server.model.Application, java.util.List, java.util.List)
	 */
	@Override
	public AuthorizeResponse authorize(
			NotesPrincipal user,
			Application app,
			List<String> grantedScopes,
			List<String> responseTypes) {
		return AuthorizeResponseBuilder.newBuilder()
				.addProperty()
					.withName("access_token")
					.withValue(this.createAccessToken(app, user, grantedScopes))
					.signedWith(this.signKey)
				.addProperty()
					.withName("token_type")
					.withValue("bearer")
				.addProperty()
					.withName("expires_in")
					.withValue(this.expiresIn)
				.build();
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.OAuthExtension#token(NotesPrincipal, Application, Object, List)
	 */
	@Override
	public TokenResponse token(
			NotesPrincipal user, 
			Application app, 
			Object context, 
			List<String> grantedScopes) {
		return null;
	}
}
