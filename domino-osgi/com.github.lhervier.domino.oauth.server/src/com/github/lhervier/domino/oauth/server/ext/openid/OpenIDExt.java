package com.github.lhervier.domino.oauth.server.ext.openid;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.entity.PersonEntity;
import com.github.lhervier.domino.oauth.server.ext.IAuthorizer;
import com.github.lhervier.domino.oauth.server.ext.IOAuthAuthorizeExtension;
import com.github.lhervier.domino.oauth.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.repo.PersonRepository;
import com.github.lhervier.domino.oauth.server.services.TimeService;

/**
 * Implémentation de OpenID par dessus OAUth2
 * @author Lionel HERVIER
 */
@Component(OpenIDExt.RESPONSE_TYPE)
public class OpenIDExt implements IOAuthAuthorizeExtension {

	public static final String RESPONSE_TYPE = "id_token";
	
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
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthAuthorizeExtension#getAuthorizedScopes()
	 */
	@Override
	public List<String> getAuthorizedScopes() {
		return Arrays.asList(SCOPE_ADDRESS, SCOPE_EMAIL, SCOPE_OPENID, SCOPE_PHONE, SCOPE_PROFILE);
	}

	/**
	 * Create an Id token
	 */
	public IdToken createIdToken(
			NotesPrincipal user, 
			Application app,
			List<String> scopes) {
		PersonEntity person = this.personRepo.findOne(user.getName());
		if( person == null )
			throw new RuntimeException("OpenID: User '" + user.getName() + "' not found in person repo. Unable to authorize.");
		
		IdToken ctx = new IdToken();
		ctx.setIat(this.timeSvc.currentTimeSeconds());
		ctx.setIss(this.iss);
		ctx.setSub(user.getName());
		ctx.setAud(app.getClientId());
		ctx.setAcr(null);				// TODO: acr non généré
		ctx.setAmr(null);				// TODO: amr non généré
		ctx.setAzp(null);				// TODO: azp non généré
		ctx.setAuthTime(this.timeSvc.currentTimeSeconds());
		if( this.request.getParameter(PARAM_NONCE) != null )
			ctx.setNonce(this.request.getParameter(PARAM_NONCE));
		else
			ctx.setNonce(null);
		
		if( scopes.contains(SCOPE_PROFILE) ) {
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
			ctx.setEmail(person.getInternetAddress());
			ctx.setEmailVerified(null);
		}
		
		if( scopes.contains(SCOPE_ADDRESS) ) {
			// FIXME: Extract street address.
			ctx.setAddress(null);
		}
		
		if( scopes.contains(SCOPE_PHONE) ) {
			ctx.setPhoneNumber(person.getOfficePhoneNumber());
			ctx.setPhoneNumberVerified(null);
		}
		
		return ctx;
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthAuthorizeExtension#authorize(com.github.lhervier.domino.oauth.server.NotesPrincipal, com.github.lhervier.domino.oauth.server.model.Application, java.util.List, com.github.lhervier.domino.oauth.server.ext.IAuthorizer)
	 */
	public void authorize(
			NotesPrincipal user,
			Application app,
			List<String> askedScopes,
			IAuthorizer authorizer) {
		if( askedScopes.contains(SCOPE_OPENID) ) {
			IdToken idToken = this.createIdToken(user, app, askedScopes);
			authorizer.addSignedProperty(TOKEN_RESPONSE_ATTR, idToken, this.signKey);
			authorizer.setContext(idToken);
			
			// authorizer.saveAuthCode(We don't care)		Let other extensions decide if the auth code must be saved
		}
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IOAuthTokenExtension#token(Object, Application, List, IPropertyAdder)
	 */
	@Override
	public void token(
			NotesPrincipal user,
			Application app,
			Object context, 
			List<String> askedScopes,
			IPropertyAdder adder) {
		IdToken idToken = this.createIdToken(user, app, askedScopes);
		if( idToken != null )
			adder.addSignedProperty(TOKEN_RESPONSE_ATTR, idToken, this.signKey);
	}
}
