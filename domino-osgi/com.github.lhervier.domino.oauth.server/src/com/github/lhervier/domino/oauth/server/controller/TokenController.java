package com.github.lhervier.domino.oauth.server.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lotus.domino.NotesException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.lhervier.domino.oauth.server.NotesUserPrincipal;
import com.github.lhervier.domino.oauth.server.aop.ann.Oauth2DbContext;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ex.GrantException;
import com.github.lhervier.domino.oauth.server.ex.InvalidUriException;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.authorize.AuthorizeServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.grant.InvalidClientException;
import com.github.lhervier.domino.oauth.server.ex.grant.InvalidGrantException;
import com.github.lhervier.domino.oauth.server.ex.grant.InvalidRequestException;
import com.github.lhervier.domino.oauth.server.ex.grant.InvalidScopeException;
import com.github.lhervier.domino.oauth.server.ex.grant.UnsupportedGrantTypeException;
import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.repo.AuthCodeRepository;
import com.github.lhervier.domino.oauth.server.repo.SecretRepository;
import com.github.lhervier.domino.oauth.server.services.AppService;
import com.github.lhervier.domino.oauth.server.utils.PropertyAdderImpl;
import com.github.lhervier.domino.oauth.server.utils.SystemUtils;
import com.github.lhervier.domino.oauth.server.utils.Utils;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;

/**
 * Bean pour le endpoint "token"
 * @author Lionel HERVIER
 */
@Controller
public class TokenController {

	/**
	 * The refresh token life time
	 */
	@Value("${oauth2.server.refreshTokenLifetime}")
	private long refreshTokenLifetime;
	
	/**
	 * Application service
	 */
	@Autowired
	private AppService appSvc;
	
	/**
	 * Authorization code repository
	 */
	@Autowired
	private AuthCodeRepository authCodeRepo;
	
	/**
	 * Secret repository
	 */
	@Autowired
	private SecretRepository secretRespo;
	
	/**
	 * The application context
	 */
	@Autowired
	private ApplicationContext springContext;
	
	// =============================================================================
	
	/**
	 * We are note able to inject this bean as a method argument
	 */
	@Autowired
	private NotesUserPrincipal tokenUser;
	
	/**
	 * Generate a token
	 * @throws GrantException error that must be serialized to the user
	 * @throws ServerErrorException main error
	 * @throws NotesException 
	 */
	@RequestMapping(value = "/token", method = RequestMethod.POST)
	@Oauth2DbContext
	public @ResponseBody Map<String, Object> token(
			@RequestParam(value = "client_id", required = false) String clientId,
			@RequestParam(value = "grant_type", required = false) String grantType,
			@RequestParam(value = "code", required = false) String code,
			@RequestParam(value = "scope", required = false) String scope,
			@RequestParam(value = "refresh_token", required = false) String refreshToken,
			@RequestParam(value = "redirect_uri", required = false) String redirectUri) throws GrantException, ServerErrorException, NotesException {
		return this.token(this.tokenUser, clientId, grantType, code, scope, refreshToken, redirectUri);
	}
	public Map<String, Object> token(
			NotesUserPrincipal user,
			String clientId,
			String grantType,
			String code,
			String scope,
			String refreshToken,
			String redirectUri) throws GrantException, ServerErrorException, NotesException {
		// Prepare response object
		Map<String, Object> resp;
		
		// Extract application from current user (the application)
		Application app = this.appSvc.getApplicationFromName(user.getCommon());
		if( app == null )
			throw new InvalidClientException("current user do not correspond to a declared application");
		
		// Validate client_id : Must be the same as the client_id associated with the current user (application)
		// As the application is authenticated, the clientId is not mandatory.
		if( StringUtils.isEmpty(clientId) )
			clientId = app.getClientId();
		if( !app.getClientId().equals(clientId) )
			throw new InvalidClientException("client_id do not correspond to the currently logged in application");
		
		// grant_type is mandatory
		if( StringUtils.isEmpty(grantType) )
			throw new InvalidRequestException();
		
		// Authorization code grant flow :
		// ================================
		if( "authorization_code".equals(grantType) ) {
			// Validate redirect_uri
			try {
				Utils.checkRedirectUri(redirectUri, app);
			} catch (InvalidUriException e) {
				throw new ServerErrorException(e.getMessage());
			}
			
			// Validate the code
			if( code == null )
				throw new InvalidRequestException("code is mandatory");
			
			// Process authorization code
			resp = this.authorizationCode(
					code,
					redirectUri,
					app
			);
		
		// Refresh grant flow :
		// =====================
		} else if( "refresh_token".equals(grantType) ) {
			
			// Extract scopes
			List<String> scopes;
			if( StringUtils.isEmpty(scope) )
				scopes = new ArrayList<String>();
			else
				scopes = Arrays.asList(StringUtils.split(scope, " "));
			
			// Refresh the token
			resp = this.refreshToken(
					app,
					refreshToken,
					scopes
			);
		} else
			throw new UnsupportedGrantTypeException("grant_type '" + grantType + "' is not supported");
		
		return resp;
	}
	
	/**
	 * Generate a new refresh token
	 * @param authCode the authorization code
	 * @return the refresh token
	 * @throws IOException 
	 * @throws JOSEException 
	 * @throws NotesException 
	 * @throws KeyLengthException 
	 */
	private String refreshTokenFromAuthCode(AuthCodeEntity authCode) throws ServerErrorException {
		try {
			JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128GCM);
			
			JSONObject payload = new JSONObject();
			payload.put("id", authCode.getId());
			payload.put("exp", SystemUtils.currentTimeSeconds() + this.refreshTokenLifetime);
			payload.put("application", authCode.getApplication());
			payload.put("clientId", authCode.getClientId());
			payload.put("redirectUri", authCode.getRedirectUri());
			
			JSONArray scopes = new JSONArray();
			for( String scope : authCode.getScopes() )
				scopes.add(scope);
			payload.put("scopes", scopes);
			
			JSONArray grantedScopes = new JSONArray();
			for( String scope : authCode.getGrantedScopes() )
				grantedScopes.add(scope);
			payload.put("grantedScopes", grantedScopes);
			
			JSONObject contexts = new JSONObject();
			for( String extId : authCode.getContextClasses().keySet() ) {
				JSONObject context = new JSONObject();
				context.put(
						"clazz", 
						authCode.getContextClasses().get(extId)
				);
				context.put(
						"jsonValue", 
						authCode.getContextObjects().get(extId)
				);
				contexts.put(extId, context);
			}
			payload.put("contexts", contexts);
			
			JWEObject jweObject = new JWEObject(
					header, 
					new Payload(payload)
			);
			jweObject.encrypt(new DirectEncrypter(
					this.secretRespo.findRefreshTokenSecret()
			));
			return jweObject.serialize();
		} catch (KeyLengthException e) {
			throw new ServerErrorException(e);
		} catch (NotesException e) {
			throw new ServerErrorException(e);
		} catch (JOSEException e) {
			throw new ServerErrorException(e);
		} catch (IOException e) {
			throw new ServerErrorException(e);
		}
	}
	
	/**
	 * Extract a refresh token
	 */
	private AuthCodeEntity authCodeFromRefreshToken(String sRefreshToken) throws ServerErrorException {
		try {
			JWEObject jweObject = JWEObject.parse(sRefreshToken);
			jweObject.decrypt(new DirectDecrypter(this.secretRespo.findRefreshTokenSecret()));
			JSONObject payload = jweObject.getPayload().toJSONObject();
			
			// Check it is not expired
			if( payload.getAsNumber("exp").longValue() < SystemUtils.currentTimeSeconds() )
				return null;
			
			AuthCodeEntity ret = new AuthCodeEntity();
			ret.setId(payload.getAsString("id"));
			ret.setApplication(payload.getAsString("application"));
			ret.setClientId(payload.getAsString("clientId"));
			ret.setRedirectUri(payload.getAsString("redirectUri"));
			
			JSONArray scopes = (JSONArray) payload.get("scopes");
			ret.setScopes(new ArrayList<String>());
			for( Object scope : scopes )
				ret.getScopes().add((String) scope);
			
			JSONArray grantedScopes = (JSONArray) payload.get("grantedScopes");
			ret.setGrantedScopes(new ArrayList<String>());
			for( Object scope : grantedScopes )
				ret.getGrantedScopes().add((String) scope);
			
			JSONObject contexts = (JSONObject) payload.get("contexts");
			ret.setContextClasses(new HashMap<String, String>());
			ret.setContextObjects(new HashMap<String, String>());
			for( String extId : contexts.keySet() ) {
				JSONObject context = (JSONObject) contexts.get(extId);
				ret.getContextClasses().put(extId, context.getAsString("clazz"));
				ret.getContextObjects().put(extId, context.getAsString("jsonValue"));
			}
			
			return ret;
		} catch (KeyLengthException e) {
			throw new ServerErrorException(e);
		} catch (NotesException e) {
			throw new ServerErrorException(e);
		} catch (JOSEException e) {
			throw new ServerErrorException(e);
		} catch (IOException e) {
			throw new ServerErrorException(e);
		} catch (ParseException e) {
			return null;
		}
	}
	
	
	/**
	 * Authorization grant flow
	 * @param code the authorization code
	 * @param redirectUri the redirection uri
	 * @param app the application
	 * @throws GrantException Error that will be serialized to the user
	 * @throws ServerErrorException main error
	 * @throws NotesException 
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> authorizationCode(
			String code, 
			String redirectUri, 
			Application app) throws GrantException, ServerErrorException, NotesException {
		try {
			// Prepare the response
			Map<String, Object> resp = new HashMap<String, Object>();
		
			// Get the authorization code
			AuthCodeEntity authCode = this.authCodeRepo.findOne(code);
			if( authCode == null )
				throw new InvalidGrantException();
			
			// Check it did not expire
			long expired = (long) authCode.getExpires();
			if( expired < SystemUtils.currentTimeSeconds() )
				throw new InvalidGrantException("code has expired");
			
			// Check it was generated for the right clientId
			if( !app.getClientId().equals(authCode.getClientId()) )
				throw new InvalidClientException("client_id is not the same as the one stored in the authorization code");
			
			// Check that the redirect_uri is the same
			if( !redirectUri.equals(authCode.getRedirectUri()) )
				throw new InvalidGrantException("redirect_uri is not the same as the one stored in the authorization code");
			
			// Make each implementation add its own properties
			// They can change their context.
			Map<String, IOAuthExtension> exts = this.springContext.getBeansOfType(IOAuthExtension.class);
			for( IOAuthExtension ext : exts.values() ) {
				Object context = Utils.getContext(authCode, ext.getId());
				if( context == null )
					continue;
				ext.token(
						context, 
						new PropertyAdderImpl(
								resp, 
								this.secretRespo
						),
						authCode.getGrantedScopes()
				);
			}
			
			// Generate the refresh token
			String refreshToken = this.refreshTokenFromAuthCode(authCode);
			resp.put("refresh_token", refreshToken);
			
			// expiration date
			resp.put("expires_in", this.refreshTokenLifetime);
			
			// token type
			resp.put("token_type", "Bearer");
			
			// scopes only if they are different from the one asked when calling authorize end point
			if( !authCode.getScopes().containsAll(authCode.getGrantedScopes()) )
				resp.put("scope", StringUtils.join(authCode.getGrantedScopes().iterator(), " "));
			
			return resp;
		} finally {
			// Remove auth code to prevend reuse
			this.authCodeRepo.delete(code);
		}
	}
	
	/**
	 * Refresh a token
	 * @param app the currently logged in user (application)
	 * @param sRefreshToken le refresh token
	 * @param scopes d'éventuels nouveaux scopes.
	 * @throws GrantException 
	 * @throws AuthorizeServerErrorException
	 * @throws NotesException
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> refreshToken(
			Application app,
			String sRefreshToken, 
			List<String> scopes) throws GrantException, ServerErrorException, NotesException {
		// Sanity check
		if( sRefreshToken == null )
			throw new InvalidGrantException("refresh_token is mandatory");
		
		// Decrypt refresh token
		AuthCodeEntity authCode = this.authCodeFromRefreshToken(sRefreshToken);
		if( authCode == null )
			throw new InvalidGrantException("refresh_token is invalid");
			
		// Check that scopes are already in the initial scopes
		if( !authCode.getGrantedScopes().containsAll(scopes) )
			throw new InvalidScopeException("scopes must be a subset of already accorded scopes");
		
		// If no scope, use the scopes originally granted by the resource owner 
		if( scopes.size() == 0 )
			scopes = authCode.getGrantedScopes();
		
		// Check that the token has been generated for the current application
		if( !app.getClientId().equals(authCode.getClientId()) )
			throw new InvalidGrantException();
		
		// Prepare the response
		Map<String, Object> resp = new HashMap<String, Object>();
		
		// Call for extensions
		Map<String, IOAuthExtension> exts = this.springContext.getBeansOfType(IOAuthExtension.class);
		for( IOAuthExtension ext : exts.values() ) {
			Object context = Utils.getContext(authCode, ext.getId());
			if( context == null )
				continue;
			ext.token(
					context, 
					new PropertyAdderImpl(
							resp,
							this.secretRespo
					), 
					scopes
			);
		}
		
		// Update scopes
		if( scopes.size() != 0 ) {
			if( !scopes.containsAll(authCode.getGrantedScopes())) {
				resp.put("scope", StringUtils.join(scopes.iterator(), " "));
				authCode.setGrantedScopes(scopes);
			}
		}
		
		// Regenerate the refresh token
		String newRefreshToken = this.refreshTokenFromAuthCode(authCode);
		resp.put("refresh_token", newRefreshToken);
		
		// Other information
		resp.put("expires_in", this.refreshTokenLifetime);
		resp.put("token_type", "Bearer");
		
		return resp;
	}
}
