package com.github.lhervier.domino.oauth.server.testsuite.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.server.ext.IScopeGranter;

@Component
public class DummyExt implements IOAuthExtension<DummyExtContext> {

	public static final String DUMMY_SCOPE = "dummy_scope";
	
	@Override
	public String getId() {
		return "dummy";
	}

	@Override
	public Class<DummyExtContext> getContextClass() {
		return DummyExtContext.class;
	}

	@Override
	public boolean validateResponseTypes(List<String> responseTypes) {
		return responseTypes.contains("code");
	}

	@Override
	public DummyExtContext initContext(NotesPrincipal user, IScopeGranter granter, String clientId, List<String> scopes) {
		if( scopes.contains(DUMMY_SCOPE) )
			granter.grant(DUMMY_SCOPE);
		return new DummyExtContext();
	}

	@Override
	public void authorize(DummyExtContext context, List<String> responseTypes, AuthCodeEntity authCode, IPropertyAdder adder) {
	}

	@Override
	public void token(DummyExtContext context, IPropertyAdder adder, AuthCodeEntity authCode) {
	}

}
