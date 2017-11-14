package com.github.lhervier.domino.oauth.server.ext.openid;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.entity.PersonEntity;
import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.server.ext.IScopeGranter;
import com.github.lhervier.domino.oauth.server.repo.PersonRepository;
import com.github.lhervier.domino.oauth.server.utils.ReflectionUtils;
import com.github.lhervier.domino.oauth.server.utils.SystemUtils;

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
	 * The sign key
	 */
	@Value("${oauth2.server.openid.signKey}")
	private String signKey;
	
	/**
	 * The http servlet request
	 */
	@Autowired
	private HttpServletRequest request;
	
	/**
	 * The Person repository
	 */
	@Autowired
	private PersonRepository personRepo;
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#getContextClass()
	 */
	@Override
	public Class<OpenIdContext> getContextClass() {
		return OpenIdContext.class;
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#validateResponseTypes(List)
	 */
	@Override
	public boolean validateResponseTypes(List<String> responseTypes) {
		return responseTypes.contains("id_token");
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#getId()
	 */
	@Override
	public String getId() {
		return "openid";
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#initContext(NotesPrincipal, IScopeGranter, String, List)
	 */
	@Override
	public OpenIdContext initContext(
			NotesPrincipal user,
			IScopeGranter granter, 
			String clientId, 
			List<String> scopes) {
		// On ne réagit que si on nous demande le scope "openid"
		if( !scopes.contains("openid") )
			return null;
		granter.grant("openid");
		
		// Les attributs par défaut
		OpenIdContext ctx = new OpenIdContext();
		ctx.setIss(this.iss);
		ctx.setSub(user.getName());
		ctx.setAud(clientId);
		ctx.setAcr(null);				// TODO: acr non généré
		ctx.setAmr(null);				// TODO: amr non généré
		ctx.setAzp(null);				// TODO: azp non généré
		ctx.setAuthTime(SystemUtils.currentTimeSeconds());
		if( this.request.getParameter("nonce") != null )
			ctx.setNonce(this.request.getParameter("nonce"));
		else
			ctx.setNonce(null);
		
		PersonEntity person = this.personRepo.findOne(user.getName());
		
		if( scopes.contains("profile") ) {
			granter.grant("profile");
			
			ctx.setName(person.getFullNames().get(0));
			ctx.setGivenName(person.getFirstName());
			ctx.setFamilyName(person.getLastName());
			ctx.setMiddleName(person.getMiddleInitial());
			ctx.setGender(person.getTitle());		// FIXME: OpenId says it should "male" or "female". We will send "Mr.", "Miss", "Dr.", etc... 
			ctx.setPreferedUsername(person.getShortName());
			ctx.setWebsite(person.getWebsite());
			ctx.setPicture(person.getPhotoUrl());
			
			ctx.setUpdatedAt(null);						// FIXME: Information is present in the "LastMod" field
			ctx.setLocale(null);						// FIXME: Preferred language is in the "preferredLanguage" field. But it's not a locale.
			
			ctx.setZoneinfo(null);						// Time zone
			ctx.setBirthdate(null);						// Date of birth
			ctx.setProfile(null);						// Profile page URL
			ctx.setNickname(null);						// "Mike" for someone called "Mickael"
		}
		
		if( scopes.contains("email") ) {
			granter.grant("email");
			ctx.setEmail(person.getInternetAddress());
			ctx.setEmailVerified(null);
		}
		
		if( scopes.contains("address") ) {
			granter.grant("address");
			// FIXME: Extract street address.
			ctx.setAddress(null);
		}
		
		if( scopes.contains("phone") ) {
			granter.grant("phone");
			ctx.setPhoneNumber(person.getOfficePhoneNumber());
			ctx.setPhoneNumberVerified(null);
		}
		
		return ctx;
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#authorize(Object, List, AuthCodeEntity, IPropertyAdder)
	 */
	@Override
	public void authorize(OpenIdContext ctx, List<String> responseTypes, AuthCodeEntity authCode, IPropertyAdder adder) {
		// Hybrid flow
		if( responseTypes.contains("id_token") ) {
			this.token(
					ctx, 
					adder, 
					authCode.getGrantedScopes()
			);
		}
	}

	/**
	 * Returns an Id token
	 */
	public IdToken createIdToken(OpenIdContext context, List<String> scopes) {
		if( !scopes.contains("openid") )
			return null;
		
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
		
		return idToken;
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#token(Object, IPropertyAdder, List)
	 */
	@Override
	public void token(
			OpenIdContext context, 
			IPropertyAdder adder,
			List<String> scopes) {
		IdToken idToken = this.createIdToken(context, scopes);
		if( idToken == null )
			return;
		
		adder.addSignedProperty("id_token", idToken, this.signKey);
	}
}
