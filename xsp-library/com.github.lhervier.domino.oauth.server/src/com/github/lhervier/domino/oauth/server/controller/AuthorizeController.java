package com.github.lhervier.domino.oauth.server.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lotus.domino.Name;
import lotus.domino.NotesException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.github.lhervier.domino.oauth.common.utils.SystemUtils;
import com.github.lhervier.domino.oauth.server.aop.ann.Oauth2DbContext;
import com.github.lhervier.domino.oauth.server.ex.AuthorizeException;
import com.github.lhervier.domino.oauth.server.ex.InvalidUriException;
import com.github.lhervier.domino.oauth.server.ex.authorize.AuthorizeServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.authorize.InvalidRequestException;
import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.ext.IScopeGranter;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.model.AuthorizationCode;
import com.github.lhervier.domino.oauth.server.services.AppService;
import com.github.lhervier.domino.oauth.server.services.SecretService;
import com.github.lhervier.domino.oauth.server.utils.PropertyAdderImpl;
import com.github.lhervier.domino.oauth.server.utils.Utils;
import com.github.lhervier.domino.spring.servlet.NotesContext;

/**
 * Authorize endpoint
 * @author Lionel HERVIER
 */
@Controller
public class AuthorizeController {

	/**
	 * To access applications
	 */
	@Autowired
	private AppService appSvc;
	
	/**
	 * The secret service
	 */
	@Autowired
	private SecretService secretSvc;
	
	/**
	 * The notes context
	 */
	@Autowired
	private NotesContext notesContext;
	
	/**
	 * The application context
	 */
	@Autowired
	private ApplicationContext springContext;
	
	/**
	 * The application root
	 */
	@Value("${oauth2.server.applicationRoot}")
	private String applicationRoot;
	
	/**
	 * Authorization codes life time
	 */
	@Value("${oauth2.server.authCodeLifetime}")
	private long authCodeLifeTime;
	
	/**
	 * Jackson object mapper
	 */
	private ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * Authorize endpoint
	 * @throws AuthorizeException If an error occur
	 * @throws InvalidUriException if the uri is invalid
	 * @throws NotesException may happend...
	 */
	@RequestMapping(value = "/authorize", method = RequestMethod.GET)
	@Oauth2DbContext
    public ModelAndView authorize(
    		@RequestParam(value = "response_type", required = false) String responseType,
    		@RequestParam(value = "client_id", required = false) String clientId,
    		@RequestParam(value = "scope", required = false) String scope,
    		@RequestParam(value = "state", required = false) String state,
    		@RequestParam(value = "redirect_uri", required = false) String redirectUri) throws AuthorizeException, InvalidUriException, NotesException, IOException {
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
		
		// Get the app name
		Name nn = null;
		String appName;
		try {
			nn = this.notesContext.getUserSession().createName(app.getName() + this.applicationRoot);
			appName = nn.toString();
		} finally {
			DominoUtils.recycleQuietly(nn);
		}
		
		// validate the redirect_uri
		Utils.checkRedirectUri(redirectUri, app);
		
		// Create an authorization code
		AuthorizationCode authCode;
		try {
			String id = Utils.generateCode();
			
			authCode = new AuthorizationCode();
			authCode.setId(id);
			authCode.setApplication(appName);
			authCode.setClientId(app.getClientId());
			authCode.setRedirectUri(redirectUri);
			authCode.setExpires(SystemUtils.currentTimeSeconds() + this.authCodeLifeTime);
			authCode.setContextClasses(new HashMap<String, String>());
			authCode.setContextObjects(new HashMap<String, String>());
			
			// Define scopes
			authCode.setScopes(scopes);
			
			// Update authorized scopes and initialize contexts
			this.initializeContexts(authCode, app, scopes);
			
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
				String key = URLEncoder.encode(entry.getKey(), "UTF-8");
				String value = URLEncoder.encode(entry.getValue().toString(), "UTF-8");
				sbRedirect.append(sep).append(key).append('=').append(value);
				sep = '&';
			}
			
			// Redirect
			return new ModelAndView(sbRedirect.toString());		
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
		Map<String, IOAuthExtension> exts = this.springContext.getBeansOfType(IOAuthExtension.class);
		for( IOAuthExtension ext : exts.values() ) {
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
	private void initializeContexts(AuthorizationCode authCode, Application app, List<String> scopes) throws NotesException, JsonGenerationException, JsonMappingException, IOException {
		final List<String> grantedScopes = new ArrayList<String>();
		Map<String, IOAuthExtension> exts = this.springContext.getBeansOfType(IOAuthExtension.class);
		for( IOAuthExtension ext : exts.values() ) {
			Object context = ext.initContext(
					this.notesContext.getUserSession(),
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
	private void runGrants(AuthorizationCode authCode, List<String> responseType, Map<String, Object> params) throws NotesException {
		Map<String, IOAuthExtension> exts = this.springContext.getBeansOfType(IOAuthExtension.class);
		for( IOAuthExtension ext : exts.values() ) {
			ext.authorize(
					Utils.getContext(authCode, ext.getId()),
					responseType, 
					authCode,
					new PropertyAdderImpl(
							params,
							this.secretSvc
					)
			);
		}
	}
}
