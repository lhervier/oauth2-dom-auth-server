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

import com.github.lhervier.domino.oauth.server.AuthorizerImpl;
import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ex.BaseAuthException;
import com.github.lhervier.domino.oauth.server.ex.InvalidUriException;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.authorize.AuthInvalidRequestException;
import com.github.lhervier.domino.oauth.server.ex.authorize.AuthServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.authorize.AuthUnsupportedResponseTypeException;
import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.repo.AuthCodeRepository;
import com.github.lhervier.domino.oauth.server.services.AppService;
import com.github.lhervier.domino.oauth.server.services.AuthorizeService;
import com.github.lhervier.domino.oauth.server.services.ExtensionService;
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
	 * Authorization codes life time
	 */
	@Value("${oauth2.server.authCodeLifetime}")
	private long authCodeLifeTime;
	
	/**
	 * Jackson object mapper
	 */
	@Autowired
	private ObjectMapper mapper;
	
	/**
	 * The authorizer
	 */
	@Autowired
	private AuthorizerImpl authorizer;
	
	// ========================================================================================================
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.AuthorizeService#authorize(com.github.lhervier.domino.oauth.server.NotesPrincipal, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public String authorize(
			NotesPrincipal user,
    		String responseType,
    		String clientId,
    		String scope,
    		String state,
    		String redirectUri) throws BaseAuthException, InvalidUriException, ServerErrorException {
		// client_id is mandatory
		if( StringUtils.isEmpty(clientId) ) 
			throw new ServerErrorException("client_id is mandatory");
		Application app = this.appSvc.getApplicationFromClientId(clientId);
		if( app == null )
			throw new ServerErrorException("client_id is invalid");
		
		// redirect_uri is mandatory (except if app only have one may redirect uri)
		if( StringUtils.isEmpty(redirectUri) && app.getRedirectUris().size() == 0 )
			redirectUri = app.getRedirectUri();
		if( StringUtils.isEmpty(redirectUri) )
			throw new InvalidUriException("redirect_uri is mandatory");
		
		// redirect_uri must be one the redirect_uris registered in the app
		if( !Utils.isRegistered(redirectUri, app) )
			throw new InvalidUriException("redirect_uri is invalid");
		
		// response_type is mandatory
		if( StringUtils.isEmpty(responseType) )
			throw new AuthInvalidRequestException("response_type is mandatory", redirectUri);
		List<String> responseTypes = new ArrayList<String>();
		{
			String[] tbl = responseType.split(" ");
			for( String r : tbl )
				responseTypes.add(r);
		}
		if( !this.extSvc.getResponseTypes().containsAll(responseTypes) )
			throw new AuthUnsupportedResponseTypeException("response_type is invalid", redirectUri);
		
		// Extract the scopes
		List<String> scopes = new ArrayList<String>();
		if( !StringUtils.isEmpty(scope) ) {
			String[] tbl = scope.split(" ");
			for( String s : tbl )
				scopes.add(s);
		}
		
		// Create an authorization code
		AuthCodeEntity authCode;
		try {
			String id = Utils.generateCode();
			
			authCode = new AuthCodeEntity();
			authCode.setId(id);
			
			authCode.setFullName(user.getName());
			authCode.setCommonName(user.getCommon());
			authCode.setRoles(user.getRoles());
			authCode.setAuthType(user.getAuthType().toString());
			authCode.setDatabasePath(user.getCurrentDatabasePath());
			
			authCode.setApplication(app.getFullName());
			authCode.setClientId(app.getClientId());
			authCode.setRedirectUri(redirectUri);
			
			authCode.setExpires(this.timeSvc.currentTimeSeconds() + this.authCodeLifeTime);
			authCode.setContextClasses(new HashMap<String, String>());
			authCode.setContextObjects(new HashMap<String, String>());
			
			// Define scopes
			authCode.setScopes(scopes);
			
			// Get granted scopes
			authCode.setGrantedScopes(new ArrayList<String>());
			for( String respType : responseTypes ) {
				IOAuthExtension ext = this.extSvc.getExtension(respType);
				for( String s : ext.getAuthorizedScopes() ) {
					if( !scopes.contains(s) )
						continue;
					if( !authCode.getGrantedScopes().contains(s) )
						authCode.getGrantedScopes().add(s);
				}
			}
			
			// Run extensions
			for( String respType : responseTypes ) {
				IOAuthExtension ext = this.extSvc.getExtension(respType);
				
				this.authorizer.setContext(null);
				ext.authorize(
						user, 
						app, 
						authCode.getGrantedScopes(), 
						this.authorizer
				);
				if( this.authorizer.getContext() == null )
					continue;
				
				authCode.getContextClasses().put(respType, this.authorizer.getContext().getClass().getName());
				authCode.getContextObjects().put(respType, this.mapper.writeValueAsString(this.authorizer.getContext()));
			}
			
			// Should we save the authorization code ?
			boolean saveAuthCode;
			if( this.authorizer.isSaveAuthCode() == null )
				saveAuthCode = false;
			else if( this.authorizer.isSaveAuthCode() )
				saveAuthCode = true;
			else
				saveAuthCode = false;
			
			// Fragment not allowed in redirect uri if auth code is to be propagated (access to token end point needed)
			if( saveAuthCode && redirectUri.indexOf('#') != -1 )
				throw new InvalidUriException("invalid redirect_uri : Auth code flow not allowed when a fragment is present in URI");
			
			// Check if we have a property conflict between extensions
			if( this.authorizer.isSaveAuthConflict() )
				throw new AuthServerErrorException("response_type conflict on grant_type: Extension conflicts on saving the authorization code", redirectUri);
			if( this.authorizer.isPropertiesConflict() )
				throw new AuthServerErrorException("response_type conflict on properties: Extension conflicts on setting properties", redirectUri);
			
			// Add the properties
			Map<String, String> params = new HashMap<String, String>();
			params.putAll(this.authorizer.getSignedProperties());
			for( Entry<String, Object> entry : this.authorizer.getProperties().entrySet() )
				params.put(entry.getKey(), entry.getValue().toString());
			params.put("state", state);		// May be null
			
			// Save auth Code if needed
			if( saveAuthCode ) {
				this.authCodeRepo.save(authCode);
				params.put("code", authCode.getId());
			}
			
			// Compute the query string
			StringBuffer sbRedirect = new StringBuffer();
			sbRedirect.append(redirectUri);
			char sep;
			if( saveAuthCode ) {
				if( redirectUri.indexOf('?') == -1 )
					sep = '?';
				else
					sep = '&';
			} else {
				if( redirectUri.indexOf('#') == -1 )
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
			
			// Redirect
			return sbRedirect.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);			// May not happen
		}
	}
}
