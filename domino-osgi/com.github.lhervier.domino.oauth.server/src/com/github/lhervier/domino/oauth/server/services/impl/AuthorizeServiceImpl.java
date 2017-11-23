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
import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.ext.IScopeGranter;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.repo.SecretRepository;
import com.github.lhervier.domino.oauth.server.services.AppService;
import com.github.lhervier.domino.oauth.server.services.AuthorizeService;
import com.github.lhervier.domino.oauth.server.utils.PropertyAdderImpl;
import com.github.lhervier.domino.oauth.server.utils.SystemUtils;
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
	 * The secret repository
	 */
	@Autowired
	private SecretRepository secretRepo;
	
	/**
	 * The extensions
	 */
	@SuppressWarnings("unchecked")
	@Autowired
	private List<IOAuthExtension> exts;
	
	/**
	 * Authorization codes life time
	 */
	@Value("${oauth2.server.authCodeLifetime}")
	private long authCodeLifeTime;
	
	/**
	 * Jackson object mapper
	 */
	private ObjectMapper mapper = new ObjectMapper();
	
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
		
		// redirect_uri is mandatory
		if( StringUtils.isEmpty(redirectUri) )
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
		if( !this.checkResponseTypes(responseTypes) )
			throw new AuthInvalidRequestException("response_type is invalid", redirectUri);
		
		// Fragment not allowed in redirect uri if code response type
		if( responseTypes.contains("code") && redirectUri.indexOf('#') != -1 )
			throw new InvalidUriException("redirect_uri must not contain a fragment to use code flow");
		
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
			authCode.setExpires(SystemUtils.currentTimeSeconds() + this.authCodeLifeTime);
			authCode.setContextClasses(new HashMap<String, String>());
			authCode.setContextObjects(new HashMap<String, String>());
			
			// Define scopes
			authCode.setScopes(scopes);
			
			// Update authorized scopes and initialize contexts
			this.initializeContexts(user, authCode, app);
			
			// Run the grants
			Map<String, Object> params = this.runGrants(authCode, responseTypes);
			
			// Add the state
			params.put("state", state);		// May be null
			
			// Compute the query string
			StringBuffer sbRedirect = new StringBuffer();
			sbRedirect.append("redirect:").append(redirectUri);
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
		} catch (JsonGenerationException e) {
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Check that ate least one extension will process the request
	 * @param responseTypes the response types
	 * @return true if ok. False otherwise
	 */
	@SuppressWarnings("unchecked")
	private boolean checkResponseTypes(List<String> responseTypes) {
		for( IOAuthExtension ext : this.exts ) {
			if( ext.validateResponseTypes(responseTypes) )
				return true;
		}
		return false;
	}
	
	/**
	 * Initialize the context
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	@SuppressWarnings("unchecked")
	private void initializeContexts(
			NotesPrincipal user,
			AuthCodeEntity authCode, 
			Application app) throws JsonGenerationException, JsonMappingException {
		final List<String> grantedScopes = new ArrayList<String>();
		for( IOAuthExtension ext : this.exts ) {
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
				try {
					authCode.getContextObjects().put(ext.getId(), this.mapper.writeValueAsString(context));
					authCode.getContextClasses().put(ext.getId(), ext.getContextClass().getName());
				} catch(IOException e) {
					throw new RuntimeException(e);			// Should not happen...
				}
			}
		}
		authCode.setGrantedScopes(grantedScopes);
	}
	
	/**
	 * Run the grants
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> runGrants(AuthCodeEntity authCode, List<String> responseType) {
		Map<String, Object> params = new HashMap<String, Object>();
		for( IOAuthExtension ext : this.exts ) {
			ext.authorize(
					Utils.getContext(authCode, ext.getId()),
					responseType, 
					authCode,
					new PropertyAdderImpl(
							params,
							this.secretRepo
					)
			);
		}
		return params;
	}
}
