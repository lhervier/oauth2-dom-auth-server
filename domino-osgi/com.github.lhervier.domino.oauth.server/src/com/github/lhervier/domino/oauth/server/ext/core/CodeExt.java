package com.github.lhervier.domino.oauth.server.ext.core;

import java.util.List;

import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.ext.AuthorizeResponse;
import com.github.lhervier.domino.oauth.server.ext.AuthorizeResponseBuilder;
import com.github.lhervier.domino.oauth.server.ext.TokenResponse;
import com.github.lhervier.domino.oauth.server.ext.TokenResponseBuilder;
import com.github.lhervier.domino.oauth.server.model.Application;

/**
 * Extension to manage "response_type=code" requests
 * @author Lionel HERVIER
 */
@Component(CodeExt.CODE_RESPONSE_TYPE)
public class CodeExt extends BaseCoreExt {

	public static final String CODE_RESPONSE_TYPE = "code";

	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.OAuthExtension#getAuthorizedScopes(List)
	 */
	@Override
	public List<String> getAuthorizedScopes(List<String> scopes) {
		return null;
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.OAuthExtension#authorize(NotesPrincipal, Application, List, List)
	 */
	@Override
	public AuthorizeResponse authorize(
			NotesPrincipal user,
			Application app,
			List<String> askedScopes,
			List<String> responseTypes) {
		return AuthorizeResponseBuilder.newBuilder()
				.addAuthCode()
				.build();
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.OAuthExtension#token(NotesPrincipal, Application, Object, List)
	 */
	public TokenResponse token(
			NotesPrincipal user,
			Application app,
			Object context,
			List<String> askedScopes) {
		AccessToken accessToken = this.createAccessToken(app, user);
		return TokenResponseBuilder.newBuilder()
				.addProperty()
					.withName("access_token")
					.withValue(accessToken)
					.signedWith(this.signKey)
				.addProperty()
					.withName("token_type")
					.withValue("bearer")
				.addProperty()
					.withName("expires_in")
					.withValue(expiresIn)
				.build();
	}
}
