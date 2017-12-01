package com.github.lhervier.domino.oauth.server.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
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
import com.github.lhervier.domino.oauth.server.ex.authorize.AuthUnsupportedResponseTypeException;
import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.ext.IScopeGranter;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.repo.SecretRepository;
import com.github.lhervier.domino.oauth.server.services.AppService;
import com.github.lhervier.domino.oauth.server.services.AuthorizeService;
import com.github.lhervier.domino.oauth.server.services.ExtensionService;
import com.github.lhervier.domino.oauth.server.services.TimeService;
import com.github.lhervier.domino.oauth.server.utils.PropertyAdderImpl;
import com.github.lhervier.domino.oauth.server.utils.Utils;

/**
 * Authorize endpoint
 * @author Lionel HERVIER
 */
@Service
public class AuthorizeServiceImpl implements AuthorizeService {

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
	 * The secret repository
	 */
	@Autowired
	private SecretRepository secretRepo;
	
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
		
		// Fragment not allowed in redirect uri if code response type
		if( responseTypes.contains("code") && redirectUri.indexOf('#') != -1 )
			throw new InvalidUriException("invalid redirect_uri : Auth code flow not allowed when a fragment is present in URI");
		
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
			authCode.setApplication(app.getFullName());
			authCode.setClientId(app.getClientId());
			authCode.setRedirectUri(redirectUri);
			authCode.setExpires(this.timeSvc.currentTimeSeconds() + this.authCodeLifeTime);
			authCode.setContextClasses(new HashMap<String, String>());
			authCode.setContextObjects(new HashMap<String, String>());
			
			// Define scopes
			authCode.setScopes(scopes);
			
			// Update authorized scopes and initialize contexts
			this.initializeContexts(user, authCode, app, responseTypes);
			
			// Run the grants
			Map<String, Object> params = this.runGrants(authCode, responseTypes);
			
			// Add the state
			params.put("state", state);		// May be null
			
			// Compute the query string
			StringBuffer sbRedirect = new StringBuffer();
			sbRedirect.append(redirectUri);
			char sep;
			if( responseTypes.contains("code") ) {
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
			for( Entry<String, Object> entry : params.entrySet() ) {
				if( entry.getValue() == null )
					continue;
				String key = Utils.urlEncode(entry.getKey());
				String value = Utils.urlEncode(entry.getValue().toString());
				sbRedirect.append(sep).append(key).append('=').append(value);
				sep = '&';
			}
			
			// Redirect
			return sbRedirect.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);			// May not happen
		}
	}
	
	/**
	 * Initialize the context
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initializeContexts(
			NotesPrincipal user,
			AuthCodeEntity authCode, 
			Application app,
			List<String> responseTypes) throws IOException {
		final List<String> grantedScopes = new ArrayList<String>();
		for( String responseType : responseTypes ) {
			IOAuthExtension ext = this.extSvc.getExtension(responseType);
			Object context = ext.initContext(
					user,
					new IScopeGranter() {
						@Override
						public void grant(String scope) {
							grantedScopes.add(scope);
						}
					}, 
					app.getClientId(), 
					authCode.getScopes()
			);
			if( context != null ) {
				authCode.getContextObjects().put(responseType, this.mapper.writeValueAsString(context));
				authCode.getContextClasses().put(responseType, ext.getContextClass().getName());
			}
		}
		authCode.setGrantedScopes(grantedScopes);
	}
	
	/**
	 * Run the grants
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, Object> runGrants(
			AuthCodeEntity authCode, 
			List<String> responseTypes) {
		Map<String, Object> params = new HashMap<String, Object>();
		for( String responseType : responseTypes ) {
			IOAuthExtension ext = this.extSvc.getExtension(responseType);
			ext.authorize(
					Utils.getContext(authCode, responseType),
					authCode,
					new PropertyAdderImpl(
							params,
							this.secretRepo,
							this.mapper
					)
			);
		}
		return params;
	}
}
