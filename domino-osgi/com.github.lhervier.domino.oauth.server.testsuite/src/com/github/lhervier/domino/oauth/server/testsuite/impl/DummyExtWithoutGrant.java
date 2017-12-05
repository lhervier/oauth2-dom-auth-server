package com.github.lhervier.domino.oauth.server.testsuite.impl;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.ext.IAuthorizer;
import com.github.lhervier.domino.oauth.server.ext.IOAuthAuthorizeExtension;
import com.github.lhervier.domino.oauth.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.server.model.Application;

@Component(DummyExtWithoutGrant.DUMMY_RESPONSE_TYPE)
public class DummyExtWithoutGrant implements IOAuthAuthorizeExtension {

	public static final String DUMMY_RESPONSE_TYPE = "dummy_nogrant";
	
	public static final String DUMMY_SCOPE = "dummy_scope";
	
	@Override
	public List<String> getAuthorizedScopes() {
		return Arrays.asList(DUMMY_SCOPE);
	}
	
	@Override
	public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, IAuthorizer authorizer) {
		DummyExtWithoutGrantContext ctx = new DummyExtWithoutGrantContext();
		ctx.setName(user.getName());
		
		authorizer.setContext(ctx);
		authorizer.addProperty("dummy_authorize_param", "authparamvalue");
		authorizer.saveAuthCode(false);
	}

	@Override
	public void token(
			NotesPrincipal user, 
			Application app, 
			Object context, 
			List<String> askedScopes,
			IPropertyAdder adder) {
		throw new RuntimeException("not supported");
	}
}
