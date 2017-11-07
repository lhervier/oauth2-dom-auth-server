package com.github.lhervier.domino.oauth.server.services;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lotus.domino.NotesException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.NotesUserPrincipal;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ex.AuthorizeException;
import com.github.lhervier.domino.oauth.server.ex.InvalidUriException;
import com.github.lhervier.domino.oauth.server.ex.authorize.AuthorizeServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.authorize.InvalidRequestException;
import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.ext.IScopeGranter;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.repo.SecretRepository;
import com.github.lhervier.domino.oauth.server.utils.PropertyAdderImpl;
import com.github.lhervier.domino.oauth.server.utils.SystemUtils;
import com.github.lhervier.domino.oauth.server.utils.Utils;

/**
 * Authorize endpoint
 * @author Lionel HERVIER
 */
@Service
public class AuthorizeService {

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
	 * Authorize endpoint.
	 * @return the redirect url.
	 * @throws AuthorizeException If an error occur
	 * @throws InvalidUriException if the uri is invalid
	 * @throws NotesException may happend...
	 */
	public String authorize(
			NotesUserPrincipal user,
    		String responseType,
    		String clientId,
    		String scope,
    		String state,
    		String redirectUri) throws AuthorizeException, InvalidUriException, NotesException, IOException {
		// response_type is mandatory
		if( StringUtils.isEmpty(responseType) )
			throw new InvalidRequestException("response_type mandatory in query string");
		List<String> responseTypes = new ArrayList<String>();
		{
			String[] tbl = responseType.split(" ");
			for( String r : tbl )
				responseTypes.add(r);
		}
		
		// Extract the scopes
		List<String> scopes = new ArrayList<String>();
		if( !StringUtils.isEmpty(scope) ) {
			String[] tbl = scope.split(" ");
			for( String s : tbl )
				scopes.add(s);
		}
		
		// Check that at least one extension will process the request
		if( !this.checkResponseTypes(responseTypes) )
			throw new InvalidRequestException("response_type value is invalid");
		
		// Client id is mandatory
		if( StringUtils.isEmpty(clientId) ) 
			throw new AuthorizeServerErrorException("client_id is mandatory");
		
		// Get the app
		Application app = this.appSvc.getApplicationFromClientId(clientId);
		if( app == null )
			throw new AuthorizeServerErrorException("unable to find app with client_id '" + clientId + "'");
		
		// validate the redirect_uri
		Utils.checkRedirectUri(redirectUri, app);
		
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
			this.initializeContexts(user, authCode, app, scopes);
			
			// Run the grants
			Map<String, Object> params = new HashMap<String, Object>();
			this.runGrants(authCode, responseTypes, params);
			
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
			} else 
				sep = '#';
			for( Entry<String, Object> entry : params.entrySet() ) {
				if( entry.getValue() == null )
					continue;
				String key = URLEncoder.encode(entry.getKey(), "UTF-8");
				String value = URLEncoder.encode(entry.getValue().toString(), "UTF-8");
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
	 * @throws NotesException 
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	@SuppressWarnings("unchecked")
	private void initializeContexts(
			NotesUserPrincipal user,
			AuthCodeEntity authCode, 
			Application app, 
			List<String> scopes) throws NotesException, JsonGenerationException, JsonMappingException, IOException {
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
					scopes
			);
			if( context != null ) {
				authCode.getContextObjects().put(ext.getId(), this.mapper.writeValueAsString(context));
				authCode.getContextClasses().put(ext.getId(), ext.getContextClass().getName());
			}
		}
		authCode.setGrantedScopes(grantedScopes);
	}
	
	/**
	 * Run the grants
	 * @throws NotesException 
	 */
	@SuppressWarnings("unchecked")
	private void runGrants(AuthCodeEntity authCode, List<String> responseType, Map<String, Object> params) throws NotesException {
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
	}
}
