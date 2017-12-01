package com.github.lhervier.domino.oauth.server.testsuite.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.server.ext.IScopeGranter;
import com.github.lhervier.domino.oauth.server.repo.AuthCodeRepository;

@Component(DummyExt.RESPONSE_TYPE)
public class DummyExt implements IOAuthExtension<DummyExtContext> {

	public static final String RESPONSE_TYPE = "dummy";
	
	public static final String DUMMY_SCOPE = "dummy_scope";
	
	@Autowired
	private AuthCodeRepository authCodeRepo;
	
	@Override
	public Class<DummyExtContext> getContextClass() {
		return DummyExtContext.class;
	}

	@Override
	public DummyExtContext initContext(NotesPrincipal user, IScopeGranter granter, String clientId, List<String> scopes) {
		if( scopes.contains(DUMMY_SCOPE) )
			granter.grant(DUMMY_SCOPE);
		return new DummyExtContext();
	}

	@Override
	public void authorize(DummyExtContext context, AuthCodeEntity authCode, IPropertyAdder adder) {
		this.authCodeRepo.save(authCode);
	}

	@Override
	public void token(DummyExtContext context, IPropertyAdder adder, AuthCodeEntity authCode) {
	}

}
