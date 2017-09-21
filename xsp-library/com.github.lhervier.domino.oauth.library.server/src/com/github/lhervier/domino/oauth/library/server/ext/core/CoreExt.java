package com.github.lhervier.domino.oauth.library.server.ext.core;

import java.util.List;

import lotus.domino.NotesException;
import lotus.domino.Session;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.common.utils.SystemUtils;
import com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.library.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.library.server.ext.IScopeGranter;

/**
 * OAUTH2 Core extension
 * @author Lionel HERVIER
 */
@Component
public class CoreExt implements IOAuthExtension<CoreContext> {

	/**
	 * The issuer
	 */
	@Value("${oauth2.server.core.iss}")
	private String iss;
	
	/**
	 * Token expiration
	 */
	@Value("${oauth2.server.core.expiresIn}")
	private long expiresIn;
	
	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#getId()
	 */
	@Override
	public String getId() {
		return "core";
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#getContextClass()
	 */
	@Override
	public Class<CoreContext> getContextClass() {
		return CoreContext.class;
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#authorize(Session, IScopeGranter, String, List)
	 */
	@Override
	public CoreContext authorize(
			Session session,
			IScopeGranter granter, 
			String clientId, 
			List<String> scopes) throws NotesException {
		CoreContext ctx = new CoreContext();
		ctx.setIss(this.iss);
		ctx.setAud(clientId);
		ctx.setSub(session.getEffectiveUserName());
		return ctx;
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#token(Object, IPropertyAdder, List)
	 */
	@Override
	public void token(
			CoreContext context, 
			IPropertyAdder adder,
			List<String> scopes) throws NotesException {
		AccessToken token = new AccessToken();
		token.setExp(SystemUtils.currentTimeSeconds() + this.expiresIn);
		token.setScopes(scopes);
		BeanUtils.copyProperties(context, token);
		adder.addSignedProperty("access_token", token);
	}
}
