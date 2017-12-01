package com.github.lhervier.domino.oauth.server.ext.core;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.server.ext.IScopeGranter;
import com.github.lhervier.domino.oauth.server.services.TimeService;

/**
 * OAUTH2 Core extension
 * @author Lionel HERVIER
 */
public abstract class BaseCoreExt implements IOAuthExtension<CoreContext> {

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
	 * The sign key
	 */
	@Value("${oauth2.server.core.signKey}")
	private String signKey;
	
	/**
	 * The time service
	 */
	@Autowired
	private TimeService timeSvc;
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#getContextClass()
	 */
	@Override
	public Class<CoreContext> getContextClass() {
		return CoreContext.class;
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#initContext(NotesPrincipal, IScopeGranter, String, List)
	 */
	@Override
	public CoreContext initContext(
			NotesPrincipal user,
			IScopeGranter granter, 
			String clientId, 
			List<String> scopes) {
		CoreContext ctx = new CoreContext();
		ctx.setIss(this.iss);
		ctx.setAud(clientId);
		ctx.setSub(user.getName());
		return ctx;
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#token(Object, IPropertyAdder, AuthCodeEntity)
	 */
	@Override
	public void token(
			CoreContext context, 
			IPropertyAdder adder,
			AuthCodeEntity authCode) {
		AccessToken token = new AccessToken();
		BeanUtils.copyProperties(context, token);
		token.setExp(this.timeSvc.currentTimeSeconds() + this.expiresIn);
		token.setScopes(authCode.getGrantedScopes());
		adder.addSignedProperty("access_token", token, this.signKey);
	}
}
