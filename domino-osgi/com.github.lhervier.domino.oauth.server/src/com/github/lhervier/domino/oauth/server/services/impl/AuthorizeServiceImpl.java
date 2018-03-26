package com.github.lhervier.domino.oauth.server.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ex.BaseAuthException;
import com.github.lhervier.domino.oauth.server.ex.InvalidUriException;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.authorize.AuthInvalidRequestException;
import com.github.lhervier.domino.oauth.server.ex.authorize.AuthServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.authorize.AuthUnsupportedResponseTypeException;
import com.github.lhervier.domino.oauth.server.ext.AuthorizeResponse;
import com.github.lhervier.domino.oauth.server.ext.OAuthExtension;
import com.github.lhervier.domino.oauth.server.ext.OAuthProperty;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.model.AuthorizeRequest;
import com.github.lhervier.domino.oauth.server.repo.AuthCodeRepository;
import com.github.lhervier.domino.oauth.server.services.AppService;
import com.github.lhervier.domino.oauth.server.services.AuthorizeService;
import com.github.lhervier.domino.oauth.server.services.ExtensionService;
import com.github.lhervier.domino.oauth.server.services.JWTService;
import com.github.lhervier.domino.oauth.server.services.TimeService;
import com.github.lhervier.domino.oauth.server.utils.Utils;

/**
 * Authorize endpoint
 * @author Lionel HERVIER
 */
@Service
public class AuthorizeServiceImpl implements AuthorizeService {

	/**
	 * Auth code repository
	 */
	@Autowired
	private AuthCodeRepository authCodeRepo;
	
	/**
	 * To access applications
	 */
	@Autowired
	private AppService appSvc;
	
	/**
	 * The time service
	 */
	@Autowired
	private TimeService timeSvc;
	
	/**
	 * The extension service
	 */
	@Autowired
	private ExtensionService extSvc;
	
	/**
	 * The JWT service
	 */
	@Autowired
	private JWTService jwtSvc;
	
	/**
	 * Authorization codes life time
	 */
	@Value("${oauth2.server.authCodeLifetime}")
	private long authCodeLifeTime;
	
	/**
	 * Jackson object mapper
	 */
	@Autowired
	private ObjectMapper mapper;
	
	// ========================================================================================================
	
	/**
	 * Check the authorize request object.
	 * WARNING: This method will update the object as needed.
	 */
	private void checkAuthRequest(AuthorizeRequest authReq) throws BaseAuthException, InvalidUriException {
		// client_id is mandatory
		if( StringUtils.isEmpty(authReq.getClientId()) ) 
			throw new ServerErrorException("client_id is mandatory");
		Application app = this.appSvc.getApplicationFromClientId(authReq.getClientId());
		if( app == null )
			throw new ServerErrorException("client_id is invalid");
		
		// redirect_uri is mandatory (except if app only have one may redirect uri)
		if( StringUtils.isEmpty(authReq.getRedirectUri()) && app.getRedirectUris().isEmpty() )
			authReq.setRedirectUri(app.getRedirectUri());
		if( StringUtils.isEmpty(authReq.getRedirectUri()) )
			throw new InvalidUriException("redirect_uri is mandatory");
		
		// redirect_uri must be one the redirect_uris registered in the app
		if( !Utils.isRegistered(authReq.getRedirectUri(), app) )
			throw new InvalidUriException("redirect_uri is invalid");
		
		// response_type is mandatory
		if( authReq.getResponseTypes().isEmpty() )
			throw new AuthInvalidRequestException("response_type is mandatory", authReq.getRedirectUri());
		if( !this.extSvc.getResponseTypes().containsAll(authReq.getResponseTypes()) )
			throw new AuthUnsupportedResponseTypeException("response_type is invalid", authReq.getRedirectUri());
	}
	
	/**
	 * Prepare an authorization code from an authorize request
	 */
	private AuthCodeEntity prepareAuthCode(NotesPrincipal user, AuthorizeRequest authReq) {
		Application app = this.appSvc.getApplicationFromClientId(authReq.getClientId());
		
		AuthCodeEntity authCode = new AuthCodeEntity();
		authCode.setId(Utils.generateCode());
		authCode.setFullName(user.getName());
		authCode.setCommonName(user.getCommon());
		authCode.setRoles(user.getRoles());
		authCode.setAuthType(user.getAuthType().toString());
		authCode.setDatabasePath(user.getCurrentDatabasePath());
		authCode.setApplication(app.getFullName());
		authCode.setClientId(app.getClientId());
		authCode.setRedirectUri(authReq.getRedirectUri());
		authCode.setExpires(this.timeSvc.currentTimeSeconds() + this.authCodeLifeTime);
		authCode.setScopes(authReq.getScopes());
		authCode.setResponseTypes(authReq.getResponseTypes());
		
		return authCode;
	}
	
	/**
	 * Get the scopes granted by the extensions for a given request
	 */
	private List<String> extractGrantedScopes(AuthorizeRequest authReq) {
		List<String> grantedScopes = new ArrayList<String>();
		for( String respType : authReq.getResponseTypes() ) {
			OAuthExtension ext = this.extSvc.getExtension(respType);
			if( ext.getAuthorizedScopes(authReq.getScopes()) == null )
				continue;
			for( String s : ext.getAuthorizedScopes(authReq.getScopes()) ) {
				if( !grantedScopes.contains(s) )
					grantedScopes.add(s);
			}
		}
		return grantedScopes;
	}
	
	/**
	 * Get the extensions responses to the authorize request
	 */
	private Map<String, AuthorizeResponse> extractExtResponses(NotesPrincipal user, AuthorizeRequest authReq, List<String> grantedScopes) {
		Application app = this.appSvc.getApplicationFromClientId(authReq.getClientId());
		Map<String, AuthorizeResponse> responses = new HashMap<String, AuthorizeResponse>();
		for( String respType : authReq.getResponseTypes() ) {
			OAuthExtension ext = this.extSvc.getExtension(respType);
			AuthorizeResponse response = ext.authorize(
					user, 
					app, 
					grantedScopes, 
					authReq.getResponseTypes()
			);
			if( response != null )
				responses.put(respType, response);
		}
		return responses;
	}
	
	/**
	 * Save a context into an auth code
	 */
	private void saveContext(AuthCodeEntity authCode, String respType, AuthorizeResponse response) {
		try {
			if( response.getContext() == null )
				return;
			
			authCode.getContextClasses().put(
					respType, 
					response.getContext().getClass().getName()
			);
			authCode.getContextObjects().put(
					respType, 
					this.mapper.writeValueAsString(response.getContext())
			);
		} catch(IOException e) {
			throw new ServerErrorException(e);
		}
	}
	
	/**
	 * Add a parameter to an existing list
	 */
	private void addParameters(AuthCodeEntity authCode, OAuthProperty prop, Map<String, String> params) throws AuthServerErrorException {
		// Multiple "code" property is allowed
		if( Utils.equals("code", prop.getName()) ) {
			params.put("code", authCode.getId());
			return;
		}
		
		// Other keys must only be extracted once per extension
		if( params.containsKey(prop.getName()) )
			throw new AuthServerErrorException("response_type conflict on properties: Extension conflicts on setting properties", authCode.getRedirectUri());
		
		// Sign the value if it is needed
		String propValue;
		if( prop.getSignKey() == null ) {
			propValue = prop.getValue().toString();
		} else {
			propValue = this.jwtSvc.createJws(prop.getValue(), prop.getSignKey());
		}
		
		// Set the value in the parameters list
		params.put(prop.getName(), propValue);
	}
	
	/**
	 * Compute the final redirect uri
	 */
	private String computeFullRedirectUri(AuthCodeEntity authCode, Map<String, String> params) {
		StringBuilder sbRedirect = new StringBuilder();
		sbRedirect.append(authCode.getRedirectUri());
		char sep;
		if( params.containsKey("code") ) {
			if( authCode.getRedirectUri().indexOf('?') == -1 )
				sep = '?';
			else
				sep = '&';
		} else {
			if( authCode.getRedirectUri().indexOf('#') == -1 )
				sep = '#';
			else
				sep = '&';
		}
		for( Entry<String, String> entry : params.entrySet() ) {
			if( entry.getValue() == null )
				continue;
			String key = Utils.urlEncode(entry.getKey());
			String value = Utils.urlEncode(entry.getValue());
			sbRedirect.append(sep).append(key).append('=').append(value);
			sep = '&';
		}
		return sbRedirect.toString();
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.AuthorizeService#authorize(NotesPrincipal, AuthorizeRequest)
	 */
	public String authorize(
			NotesPrincipal user,
    		AuthorizeRequest authReq) throws BaseAuthException, InvalidUriException {
		// Check auth request
		this.checkAuthRequest(authReq);
		
		// Prepare an authorization code
		AuthCodeEntity authCode = this.prepareAuthCode(user, authReq);
		
		// Extract the granted scopes from the extensions
		authCode.setGrantedScopes(this.extractGrantedScopes(authReq));
		
		// Run extensions
		Map<String, AuthorizeResponse> responses = this.extractExtResponses(user, authReq, authCode.getGrantedScopes());
		
		// Save extension contexts into auth code
		for( Entry<String, AuthorizeResponse> entry : responses.entrySet() ) {
			String respType = entry.getKey();
			AuthorizeResponse response = entry.getValue();
			
			// Save the context into the auth code
			this.saveContext(authCode, respType, response);
		}
		
		// Extract query parameters from extensions answers
		Map<String, String> params = new HashMap<String, String>();
		for( AuthorizeResponse response : responses.values() ) {
			for( OAuthProperty prop : response.getProperties().values() )
				this.addParameters(authCode, prop, params);
		}
		
		// Not allowed to send code into URL with fragment
		if( params.containsKey("code") && authReq.getRedirectUri().contains("#") )
			throw new InvalidUriException("invalid redirect_uri : Auth code flow not allowed when a fragment is present in URI");
		
		// Don't forget the state
		params.put("state", authReq.getState());		// May be null
		
		// Save auth Code if needed
		if( params.containsKey("code") )
			this.authCodeRepo.save(authCode);
		
		// If granted scopes are different from asked scopes, then add scope parameter
		if( !params.containsKey("code") && !authCode.getGrantedScopes().containsAll(authReq.getScopes()) )
			params.put("scope", StringUtils.join(authCode.getGrantedScopes().iterator(), ' '));
		
		// Compute the full redirect uri
		return this.computeFullRedirectUri(authCode, params);
	}
}
