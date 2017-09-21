package com.github.lhervier.domino.oauth.library.server.ext.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.github.lhervier.domino.oauth.common.utils.ReflectionUtils;
import com.github.lhervier.domino.oauth.common.utils.SystemUtils;
import com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.library.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.library.server.ext.IScopeGranter;
import com.github.lhervier.domino.oauth.library.server.ext.openid.OpenIdContext;

/**
 * Implémentation de OpenID par dessus OAUth2
 * @author Lionel HERVIER
 */
@Component
public class OpenIDExt implements IOAuthExtension<OpenIdContext> {

	/**
	 * The issuer
	 */
	@Value("${oauth2.server.openid.iss}")
	private String iss;
	
	/**
	 * Id token expiration
	 */
	@Value("${oauth2.server.openid.expiresIn}")
	private long expiresIn;
	
	/**
	 * The http servlet request
	 */
	@Autowired
	private HttpServletRequest request;
	
	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#getContextClass()
	 */
	@Override
	public Class<OpenIdContext> getContextClass() {
		return OpenIdContext.class;
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#getId()
	 */
	@Override
	public String getId() {
		return "openid";
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#authorize(Session, IScopeGranter, String, List)
	 */
	@Override
	public OpenIdContext authorize(
			Session session,
			IScopeGranter granter, 
			String clientId, 
			List<String> scopes) throws NotesException {
		// On ne réagit que si on nous demande le scope "openid"
		if( !scopes.contains("openid") )
			return null;
		granter.grant("openid");
		
		// Les attributs par défaut
		OpenIdContext ctx = new OpenIdContext();
		ctx.setIss(this.iss);
		ctx.setSub(session.getEffectiveUserName());
		ctx.setAud(clientId);
		ctx.setExp(SystemUtils.currentTimeSeconds() + this.expiresIn);
		ctx.setAcr("");				// TODO: acr non généré
		ctx.setAmr("");				// TODO: amr non généré
		ctx.setAzp("");				// TODO: azp non généré
		ctx.setAuthTime(SystemUtils.currentTimeSeconds());
		if( this.request.getParameter("nonce") != null )
			ctx.setNonce(this.request.getParameter("nonce"));
		else
			ctx.setNonce(null);
		
		if( scopes.contains("profile") ) {
			granter.grant("profile");
			
			Name nn = null;
			try {
				nn = session.createName(session.getEffectiveUserName());
				ctx.setName(nn.getCommon());
			} finally {
				DominoUtils.recycleQuietly(nn);
			}
			// TODO: Récupérer ces infos depuis la fiche du NAB !!
			ctx.setFamilyName("");
			ctx.setGivenName("");
			ctx.setMiddleName("");
			ctx.setNickname("");
			ctx.setPreferedUsername("");
			ctx.setProfile("");
			ctx.setPicture("");
			ctx.setWebsite("");
			ctx.setGender("");
			ctx.setBirthdate("");
			ctx.setZoneinfo("");
			ctx.setLocale("");
			ctx.setUpdatedAt("");
		}
		
		if( scopes.contains("email") ) {
			granter.grant("email");
			ctx.setEmail("");
			ctx.setEmailVerified("");
		}
		
		if( scopes.contains("address") ) {
			granter.grant("address");
			ctx.setAddress("");
		}
		
		if( scopes.contains("phone") ) {
			granter.grant("phone");
			ctx.setPhoneNumber("");
			ctx.setPhoneNumberVerified("");
		}
		
		return ctx;
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#token(Object, IPropertyAdder, List)
	 */
	@Override
	public void token(
			OpenIdContext context, 
			IPropertyAdder adder,
			List<String> scopes) {
		if( !scopes.contains("openid") )
			return;
		
		IdToken idToken = new IdToken();
		idToken.setIat(SystemUtils.currentTimeSeconds());
		
		// Main properties
		ReflectionUtils.copyProperties(
				context, 
				idToken, 
				new String[] {"iss", "sub", "aud", "exp", "acr", "amr", "azp", "authTime", "nonce"}
		);
		
		// Profile properties
		if( scopes.contains("profile") ) {
			ReflectionUtils.copyProperties(
					context, 
					idToken, 
					new String[] {"name", "familyName", "givenName", "middleName", "nickname", "preferedUsername", "profile", "picture", "website", "gender", "birthdate", "zoneinfo", "locale", "updatedAt"}
			);
		}
		
		// Email properties
		if( scopes.contains("email") ) {
			ReflectionUtils.copyProperties(
					context, 
					idToken, 
					new String[] {"email", "emailVerified"}
			);
		}
		
		// Address properties
		if( scopes.contains("address") ) {
			ReflectionUtils.copyProperties(
					context, 
					idToken, 
					new String[] {"address"}
			);
		}
		
		// Phone properties
		if( scopes.contains("phone") ) {
			ReflectionUtils.copyProperties(
					context, 
					idToken, 
					new String[] {"phoneNumber", "phoneNumberVerified"}
			);
		}
		
		adder.addSignedProperty("id_token", idToken);
	}
}
