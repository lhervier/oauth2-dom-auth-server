package com.github.lhervier.domino.oauth.server.ext.core;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.ext.AuthorizeResponse;
import com.github.lhervier.domino.oauth.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.server.model.Application;

/**
 * Extension to manage "response_type=code" requests
 * @author Lionel HERVIER
 */
@Component(CodeExt.CODE_RESPONSE_TYPE)
public class CodeExt extends BaseCoreExt {

	public static final String CODE_RESPONSE_TYPE = "code";

	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#getAuthorizedScopes()
	 */
	@Override
	public List<String> getAuthorizedScopes() {
		return new ArrayList<String>();
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#authorize(NotesPrincipal, Application, String, List, List)
	 */
	@Override
	public AuthorizeResponse authorize(
			NotesPrincipal user,
			Application app,
			List<String> askedScopes,
			List<String> responseTypes) {
		return AuthorizeResponse.init()
				.addAuthCode()
				.build();
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
		AccessToken accessToken = this.createAccessToken(app, user);
		adder.addSignedProperty("access_token", accessToken, this.signKey);
		adder.addProperty("token_type", "bearer");
	}
}
