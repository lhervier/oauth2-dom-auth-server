package com.github.lhervier.domino.oauth.server.testsuite.impl;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.ext.IAuthorizer;
import com.github.lhervier.domino.oauth.server.ext.IOAuthAuthorizeExtension;
import com.github.lhervier.domino.oauth.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.server.model.Application;

@Component(DummyExtWithGrant.DUMMY_RESPONSE_TYPE)
public class DummyExtWithGrant implements IOAuthAuthorizeExtension {

	public static final String DUMMY_RESPONSE_TYPE = "dummy";
	
	public static final String DUMMY_SCOPE = "dummy_scope";
	
	@Override
	public List<String> getAuthorizedScopes() {
		return Arrays.asList(DUMMY_SCOPE);
	}
	
	@Override
	public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, IAuthorizer authorizer) {
		DummyExtWithGrantContext ctx = new DummyExtWithGrantContext();
		ctx.setName(user.getName());
		
		authorizer.setContext(ctx);
		authorizer.addProperty("dummy_authorize_param", "authparamvalue");
		authorizer.saveAuthCode(true);
	}

	@Override
	public void token(
			NotesPrincipal user, 
			Application app, 
			Object context, 
			List<String> askedScopes,
			IPropertyAdder adder) {
		DummyExtWithGrantContext ctx = (DummyExtWithGrantContext) context;
		adder.addProperty("dummy_token_param", "tokenparamvalue");
		adder.addProperty("dummy_user", ctx.getName());
	}
}
