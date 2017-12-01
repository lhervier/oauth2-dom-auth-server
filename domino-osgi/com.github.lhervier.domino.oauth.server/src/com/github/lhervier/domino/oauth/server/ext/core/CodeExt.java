package com.github.lhervier.domino.oauth.server.ext.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.server.repo.AuthCodeRepository;

/**
 * OAUTH2 Core extension
 * @author Lionel HERVIER
 */
@Component(CodeExt.RESPONSE_TYPE)
public class CodeExt extends BaseCoreExt {

	public static final String RESPONSE_TYPE = "code";
	
	/**
	 * The authorization code repository
	 */
	@Autowired
	private AuthCodeRepository authCodeRepo;
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#getContextClass()
	 */
	@Override
	public Class<CoreContext> getContextClass() {
		return CoreContext.class;
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#authorize(Object, AuthCodeEntity, IPropertyAdder)
	 */
	@Override
	public void authorize(
			CoreContext ctx,
			AuthCodeEntity authCode,
			IPropertyAdder adder) {
		// Save the authorization code
		AuthCodeEntity saved = this.authCodeRepo.save(authCode);
		
		// Add the code to the query string
		adder.addProperty("code", saved.getId());
	}
}
