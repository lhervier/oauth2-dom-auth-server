package com.github.lhervier.domino.oauth.server.ext.core;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.server.ext.IScopeGranter;
import com.github.lhervier.domino.oauth.server.repo.AuthCodeRepository;
import com.github.lhervier.domino.oauth.server.utils.SystemUtils;

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
	 * The sign key
	 */
	@Value("${oauth2.server.core.signKey}")
	private String signKey;
	
	/**
	 * The authorization code repository
	 */
	@Autowired
	private AuthCodeRepository authCodeRepo;
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#getId()
	 */
	@Override
	public String getId() {
		return "core";
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#getContextClass()
	 */
	@Override
	public Class<CoreContext> getContextClass() {
		return CoreContext.class;
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#validateResponseTypes(List)
	 */
	@Override
	public boolean validateResponseTypes(List<String> responseTypes) {
		// Authorization code flow
		if( responseTypes.contains("code") )
			return true;
		// Implicit flow
		if( responseTypes.contains("token") )
			return true;
		// Otherwise, not supported
		return false;
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
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#authorize(Object, List, AuthCodeEntity, IPropertyAdder)
	 */
	@Override
	public void authorize(
			CoreContext ctx,
			List<String> responseTypes, 
			AuthCodeEntity authCode,
			IPropertyAdder adder) {
		// Authorization code grant
		if( responseTypes.contains("code") ) {
			
			// Save the authorization code
			AuthCodeEntity saved = this.authCodeRepo.save(authCode);
			
			// Add the code to the query string
			adder.addProperty("code", saved.getId());
		}
		
		// Implicit grant
		if( responseTypes.contains("token") ) {
			this.token(
					ctx, 
					adder, 
					authCode.getGrantedScopes()
			);
		}
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#token(Object, IPropertyAdder, List)
	 */
	@Override
	public void token(
			CoreContext context, 
			IPropertyAdder adder,
			List<String> scopes) {
		AccessToken token = new AccessToken();
		BeanUtils.copyProperties(context, token);
		token.setExp(SystemUtils.currentTimeSeconds() + this.expiresIn);
		token.setScopes(scopes);
		adder.addSignedProperty("access_token", token, this.signKey);
	}
}
