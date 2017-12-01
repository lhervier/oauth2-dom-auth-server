package com.github.lhervier.domino.oauth.server.ext.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ext.IPropertyAdder;

/**
 * OAUTH2 Core extension
 * @author Lionel HERVIER
 */
@Component(TokenExt.RESPONSE_TYPE)
public class TokenExt extends BaseCoreExt {

	public static final String RESPONSE_TYPE = "token";
	
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
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#authorize(Object, AuthCodeEntity, IPropertyAdder)
	 */
	@Override
	public void authorize(
			CoreContext ctx,
			AuthCodeEntity authCode,
			IPropertyAdder adder) {
		this.token(
				ctx, 
				adder, 
				authCode
		);
	}
}
