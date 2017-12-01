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
import com.github.lhervier.domino.oauth.server.services.TimeService;
import com.github.lhervier.domino.oauth.server.utils.ReflectionUtils;

/**
 * Implémentation de OpenID par dessus OAUth2
 * @author Lionel HERVIER
 */
@Component(OpenIDExt.RESPONSE_TYPE)
public class OpenIDExt implements IOAuthExtension<OpenIdContext> {

	public static final String RESPONSE_TYPE = "openid";
	
	public final static String PARAM_NONCE = "nonce";
	
	public static final String TOKEN_RESPONSE_ATTR = "id_token";
	
	public static final String SCOPE_OPENID = "openid";
	public static final String SCOPE_PROFILE = "profile";
	public static final String SCOPE_EMAIL = "email";
	public static final String SCOPE_ADDRESS = "address";
	public static final String SCOPE_PHONE = "phone";
	
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
	 * Time service
	 */
	@Autowired
	private TimeService timeSvc;
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#getContextClass()
	 */
	@Override
	public Class<OpenIdContext> getContextClass() {
		return OpenIdContext.class;
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
		if( !scopes.contains(SCOPE_OPENID) )
			return null;
		granter.grant(SCOPE_OPENID);
		
		// Les attributs par défaut
		OpenIdContext ctx = new OpenIdContext();
		ctx.setIss(this.iss);
		ctx.setSub(user.getName());
		ctx.setAud(clientId);
		ctx.setAcr(null);				// TODO: acr non généré
		ctx.setAmr(null);				// TODO: amr non généré
		ctx.setAzp(null);				// TODO: azp non généré
		ctx.setAuthTime(this.timeSvc.currentTimeSeconds());
		if( this.request.getParameter(PARAM_NONCE) != null )
			ctx.setNonce(this.request.getParameter(PARAM_NONCE));
		else
			ctx.setNonce(null);
		
		PersonEntity person = this.personRepo.findOne(user.getName());
		
		if( scopes.contains(SCOPE_PROFILE) ) {
			granter.grant(SCOPE_PROFILE);
			
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
		
		if( scopes.contains(SCOPE_EMAIL) ) {
			granter.grant(SCOPE_EMAIL);
			ctx.setEmail(person.getInternetAddress());
			ctx.setEmailVerified(null);
		}
		
		if( scopes.contains(SCOPE_ADDRESS) ) {
			granter.grant(SCOPE_ADDRESS);
			// FIXME: Extract street address.
			ctx.setAddress(null);
		}
		
		if( scopes.contains(SCOPE_PHONE) ) {
			granter.grant(SCOPE_PHONE);
			ctx.setPhoneNumber(person.getOfficePhoneNumber());
			ctx.setPhoneNumberVerified(null);
		}
		
		return ctx;
	}

	/**
	 * Returns an Id token
	 */
	public IdToken createIdToken(OpenIdContext context, List<String> scopes) {
		if( !scopes.contains(SCOPE_OPENID) )
			return null;
		
		IdToken idToken = new IdToken();
		idToken.setIat(this.timeSvc.currentTimeSeconds());
		
		// Main properties
		ReflectionUtils.copyProperties(
				context, 
				idToken, 
				new String[] {"iss", "sub", "aud", "exp", "acr", "amr", "azp", "authTime", "nonce"}
		);
		
		// Profile properties
		if( scopes.contains(SCOPE_PROFILE) ) {
			ReflectionUtils.copyProperties(
					context, 
					idToken, 
					new String[] {"name", "familyName", "givenName", "middleName", "nickname", "preferedUsername", "profile", "picture", "website", "gender", "birthdate", "zoneinfo", "locale", "updatedAt"}
			);
		}
		
		// Email properties
		if( scopes.contains(SCOPE_EMAIL) ) {
			ReflectionUtils.copyProperties(
					context, 
					idToken, 
					new String[] {"email", "emailVerified"}
			);
		}
		
		// Address properties
		if( scopes.contains(SCOPE_ADDRESS) ) {
			ReflectionUtils.copyProperties(
					context, 
					idToken, 
					new String[] {"address"}
			);
		}
		
		// Phone properties
		if( scopes.contains(SCOPE_PHONE) ) {
			ReflectionUtils.copyProperties(
					context, 
					idToken, 
					new String[] {"phoneNumber", "phoneNumberVerified"}
			);
		}
		
		return idToken;
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#authorize(Object, AuthCodeEntity, IPropertyAdder)
	 */
	@Override
	public void authorize(
			OpenIdContext ctx,  
			AuthCodeEntity authCode, 
			IPropertyAdder adder) {
		this.token(
				ctx, 
				adder, 
				authCode
		);
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthExtension#token(Object, IPropertyAdder, List)
	 */
	@Override
	public void token(
			OpenIdContext context, 
			IPropertyAdder adder,
			AuthCodeEntity authCode) {
		IdToken idToken = this.createIdToken(context, authCode.getGrantedScopes());
		if( idToken == null )
			return;
		
		adder.addSignedProperty(TOKEN_RESPONSE_ATTR, idToken, this.signKey);
	}
}
