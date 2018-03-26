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
	 * @see com.github.lhervier.domino.oauth.server.services.AuthorizeService#authorize(com.github.lhervier.domino.oauth.server.NotesPrincipal, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public String authorize(
			NotesPrincipal user,
    		String responseType,
    		String clientId,
    		String scope,
    		String state,
    		String redirectUri) throws BaseAuthException, InvalidUriException {
		// client_id is mandatory
		if( StringUtils.isEmpty(clientId) ) 
			throw new ServerErrorException("client_id is mandatory");
		Application app = this.appSvc.getApplicationFromClientId(clientId);
		if( app == null )
			throw new ServerErrorException("client_id is invalid");
		
		// redirect_uri is mandatory (except if app only have one may redirect uri)
		if( StringUtils.isEmpty(redirectUri) && app.getRedirectUris().isEmpty() )
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
		for( String r : responseType.split(" ") )
			responseTypes.add(r);
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
			
			// Define scopes
			authCode.setScopes(scopes);
			
			// Keep response types in auth code
			authCode.setResponseTypes(responseTypes);
			
			// Get granted scopes
			authCode.setGrantedScopes(new ArrayList<String>());
			for( String respType : responseTypes ) {
				OAuthExtension ext = this.extSvc.getExtension(respType);
				if( ext.getAuthorizedScopes(scopes) == null )
					continue;
				for( String s : ext.getAuthorizedScopes(scopes) ) {
					if( !authCode.getGrantedScopes().contains(s) )
						authCode.getGrantedScopes().add(s);
				}
			}
			
			// Run extensions
			Map<String, String> params = new HashMap<String, String>();
			for( String respType : responseTypes ) {
				OAuthExtension ext = this.extSvc.getExtension(respType);
				
				AuthorizeResponse response = ext.authorize(
						user, 
						app, 
						authCode.getGrantedScopes(), 
						responseTypes
				);
				if( response == null )
					continue;
				
				// Persist an eventual context
				if( response.getContext() != null ) {
					authCode.getContextClasses().put(
							respType, 
							response.getContext().getClass().getName()
					);
					authCode.getContextObjects().put(
							respType, 
							this.mapper.writeValueAsString(response.getContext())
					);
				}
				
				// Save properties, detecting conflicts
				for( OAuthProperty prop : response.getProperties().values() ) {
					if( Utils.equals("code", prop.getName()) ) {
						params.put("code", authCode.getId());		// May be multiple times...
					} else if( params.containsKey(prop.getName()) ) {
						throw new AuthServerErrorException("response_type conflict on properties: Extension conflicts on setting properties", redirectUri);
					} else if( prop.getSignKey() == null ) {
						params.put(prop.getName(), prop.getValue().toString());
					} else {
						params.put(prop.getName(), this.jwtSvc.createJws(prop.getValue(), prop.getSignKey()));
					}
				}
			}
			
			// Not sending code into URL with fragment
			if( params.containsKey("code") && redirectUri.contains("#") )
				throw new InvalidUriException("invalid redirect_uri : Auth code flow not allowed when a fragment is present in URI");
			
			// Don't forget the state
			params.put("state", state);		// May be null
			
			// Save auth Code if needed
			if( params.containsKey("code") )
				this.authCodeRepo.save(authCode);
			
			// If granted scopes are different from asked scopes, then add scope parameter
			if( !params.containsKey("code") ) {
				if( !authCode.getGrantedScopes().containsAll(scopes) )
					params.put("scope", StringUtils.join(authCode.getGrantedScopes().iterator(), ' '));
			}
			
			// Compute the query string
			StringBuffer sbRedirect = new StringBuffer();
			sbRedirect.append(redirectUri);
			char sep;
			if( params.containsKey("code") ) {
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
			throw new ServerErrorException(e);			// May not happen
		}
	}
}
