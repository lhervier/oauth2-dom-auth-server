package com.github.lhervier.domino.oauth.server.testsuite.impl;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.ext.IAuthorizer;
import com.github.lhervier.domino.oauth.server.ext.IOAuthAuthorizeExtension;
import com.github.lhervier.domino.oauth.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.server.model.Application;

/**
 * This extension conflict on properties with {@link DummyExtWithGrant}
 * @author Lionel HERVIER
 */
@Component(ConflictExt.CONFLICT_RESPONSE_TYPE)
public class ConflictExt implements IOAuthAuthorizeExtension {

	public static final String CONFLICT_RESPONSE_TYPE = "conflict";
	
	public static final String DUMMY_SCOPE = "dummy_scope";
	
	@Override
	public List<String> getAuthorizedScopes() {
		return Arrays.asList(DUMMY_SCOPE);
	}
	
	@Override
	public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, IAuthorizer authorizer) {
		ConflictExtContext ctx = new ConflictExtContext();
		ctx.setName(user.getName());
		authorizer.setContext(ctx);
		authorizer.addProperty("dummy_authorize_param", "authparamvalue");
	}

	@Override
	public void token(
			NotesPrincipal user, 
			Application app, 
			Object context, 
			List<String> askedScopes,
			IPropertyAdder adder) {
		ConflictExtContext ctx = (ConflictExtContext) context;
		adder.addProperty("dummy_token_param", "tokenparamvalue");
		adder.addProperty("dummy_user", ctx.getName());
	}
}
