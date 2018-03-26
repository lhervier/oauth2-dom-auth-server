package com.github.lhervier.domino.oauth.server.ext.openid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.entity.PersonEntity;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.server.ext.AuthorizeResponse;
import com.github.lhervier.domino.oauth.server.ext.AuthorizeResponseBuilder;
import com.github.lhervier.domino.oauth.server.ext.OAuthExtension;
import com.github.lhervier.domino.oauth.server.ext.TokenResponse;
import com.github.lhervier.domino.oauth.server.ext.TokenResponseBuilder;
import com.github.lhervier.domino.oauth.server.ext.core.TokenExt;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.repo.PersonRepository;
import com.github.lhervier.domino.oauth.server.services.TimeService;

/**
 * Implémentation de OpenID par dessus OAUth2
 * @author Lionel HERVIER
 */
@Component(OpenIDExt.RESPONSE_TYPE)
public class OpenIDExt implements OAuthExtension {

	public static final String RESPONSE_TYPE = "id_token";
	
	public static final String PARAM_NONCE = "nonce";
	
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
	 * Expiration of the id_token
	 */
	@Value("${oauth2.server.openid.expiresIn}")
	private long expiresIn;
	
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
	 * @see com.github.lhervier.domino.oauth.server.ext.OAuthExtension#getAuthorizedScopes(List)
	 */
	@Override
	public List<String> getAuthorizedScopes(List<String> scopes) {
		if( !scopes.contains(SCOPE_OPENID) )
			return Collections.emptyList();
		
		List<String> ret = new ArrayList<String>();
		ret.add(SCOPE_OPENID);
		if( scopes.contains(SCOPE_ADDRESS) ) ret.add(SCOPE_ADDRESS);
		if( scopes.contains(SCOPE_EMAIL) ) ret.add(SCOPE_EMAIL);
		if( scopes.contains(SCOPE_PHONE) ) ret.add(SCOPE_PHONE);
		if( scopes.contains(SCOPE_PROFILE) ) ret.add(SCOPE_PROFILE);
		return ret;
	}

	/**
	 * Create an Id token
	 */
	public IdToken createIdToken(
			NotesPrincipal user, 
			Application app,
			List<String> scopes,
			String nonce) {
		PersonEntity person = this.personRepo.findOne(user.getName());
		if( person == null )
			throw new ServerErrorException("OpenID: User '" + user.getName() + "' not found in person repo. Unable to authorize.");
		
		IdToken ctx = new IdToken();
		ctx.setExpires(this.timeSvc.currentTimeSeconds() + this.expiresIn);
		ctx.setIat(this.timeSvc.currentTimeSeconds());
		ctx.setIss(this.iss);
		ctx.setSub(user.getName());
		ctx.setAud(app.getClientId());
		ctx.setAcr(null);				// TODO: acr non généré
		ctx.setAmr(null);				// TODO: amr non généré
		ctx.setAzp(null);				// TODO: azp non généré
		ctx.setAuthTime(this.timeSvc.currentTimeSeconds());
		ctx.setNonce(nonce);
		
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
	 * @see com.github.lhervier.domino.oauth.server.ext.OAuthExtension#authorize(NotesPrincipal, Application, List, List)
	 */
	public AuthorizeResponse authorize(
			NotesPrincipal user,
			Application app,
			List<String> askedScopes,
			List<String> responseTypes) {
		if( !askedScopes.contains(SCOPE_OPENID) )
			return null;
		
		OpenIDContext ctx = new OpenIDContext();
		ctx.setAuthTime(timeSvc.currentTimeSeconds());
		ctx.setNonce(this.request.getParameter(PARAM_NONCE));
		
		AuthorizeResponseBuilder builder = AuthorizeResponseBuilder.newBuilder().setContext(ctx);
		if( responseTypes.contains(TokenExt.TOKEN_RESPONSE_TYPE) ) {
			builder.addProperty()
				.withName(TOKEN_RESPONSE_ATTR)
					.withValue(this.createIdToken(
							user, 
							app, 
							askedScopes, 
							this.request.getParameter(PARAM_NONCE)
					))
					.signedWith(this.signKey);
		}
		return builder.build();
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.OAuthExtension#token(NotesPrincipal, Application, Object, List)
	 */
	@Override
	public TokenResponse token(
			NotesPrincipal user,
			Application app,
			Object context, 
			List<String> grantedScopes) {
		if( context == null )
			return null;
		OpenIDContext oidCtx = (OpenIDContext) context;
		
		IdToken idToken = this.createIdToken(user, app, grantedScopes, oidCtx.getNonce());
		idToken.setAuthTime(oidCtx.getAuthTime());
		
		return TokenResponseBuilder.newBuilder()
				.addProperty()
					.withName(TOKEN_RESPONSE_ATTR)
					.withValue(idToken)
					.signedWith(this.signKey)
				.build();
	}
}
