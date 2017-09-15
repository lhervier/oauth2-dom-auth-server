package com.github.lhervier.domino.oauth.library.server.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lotus.domino.NotesException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.github.lhervier.domino.oauth.common.model.StateResponse;
import com.github.lhervier.domino.oauth.common.utils.QueryStringUtils;
import com.github.lhervier.domino.oauth.library.server.aop.ann.Oauth2DbContext;
import com.github.lhervier.domino.oauth.library.server.ex.AuthorizeException;
import com.github.lhervier.domino.oauth.library.server.ex.InvalidUriException;
import com.github.lhervier.domino.oauth.library.server.ex.authorize.InvalidRequestException;
import com.github.lhervier.domino.oauth.library.server.ex.authorize.AuthorizeServerErrorException;
import com.github.lhervier.domino.oauth.library.server.ex.authorize.UnsupportedResponseTypeException;
import com.github.lhervier.domino.oauth.library.server.model.Application;
import com.github.lhervier.domino.oauth.library.server.model.AuthorizationCode;
import com.github.lhervier.domino.oauth.library.server.model.AuthorizationCodeResponse;
import com.github.lhervier.domino.oauth.library.server.services.AppService;
import com.github.lhervier.domino.oauth.library.server.services.AuthCodeService;

/**
 * Authorize endpoint
 * @author Lionel HERVIER
 */
@Controller
public class AuthorizeController extends BaseController {

	/**
	 * To access applications
	 */
	@Autowired
	private AppService appSvc;
	
	/**
	 * The authorization code service
	 */
	@Autowired
	private AuthCodeService authCodeSvc;
	
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
    		@RequestParam(value = "state", required = false) String state) throws AuthorizeException, InvalidUriException, NotesException {
		// response_type is mandatory
		if( StringUtils.isEmpty(responseType) )
			throw new InvalidRequestException("response_type mandatory in query string");
		
		// redirect_uri is optional ?? (see https://tools.ietf.org/html/rfc6749#section-4.1.1)
		String redirectUri;
		
		// Authorization Code Grant
		// ===========================
		StateResponse ret;
		if( "code".equals(responseType) ) {
			// client_id is mandatory
			if( StringUtils.isEmpty(clientId) )
				throw new InvalidRequestException("client_id mandatory in query string.");
			
			// Le redirectUri est obligatoire
			redirectUri = this.getRedirectUri();
			
			// Exécution
			List<String> scopes;
			if( StringUtils.isEmpty(scope) )
				scopes = new ArrayList<String>();
			else
				scopes = Arrays.asList(StringUtils.split(scope, " "));
			ret = this.authorizationCode(
					clientId, 
					redirectUri, 
					scopes
			);
		// TODO: Implement other grant flows
		} else
			throw new UnsupportedResponseTypeException("response_type '" + responseType + "' is invalid");
	
		// Add the state
		ret.setState(state);		// May be null
		
		// Redirect
		return new ModelAndView("redirect:" + QueryStringUtils.addBeanToQueryString(redirectUri, ret));
	}
	
	// ===========================================================================================================
	
	/**
	 * Authorization code grant flow
	 * @param clientId l'id du client
	 * @param redirectUri l'uri de redirection
	 * @param scopes les scopes FIXME: Il ne sont pas utilisés
	 * @return lhe authorize response
	 * @throws AuthorizeException If an error occur
	 * @throws InvalidUriException si l'uri est invalide
	 * @throws NotesException  
	 */
	private AuthorizationCodeResponse authorizationCode(
			String clientId, 
			String redirectUri,
			List<String> scopes) throws AuthorizeException, InvalidUriException, NotesException {
		// Récupère l'application
		Application app = this.appSvc.getApplicationFromClientId(clientId);
		if( app == null )
			throw new AuthorizeServerErrorException("unable to find app with client_id '" + clientId + "'");
		
		// Vérifie que l'uri de redirection est bien dans la liste
		Set<String> redirectUris = new HashSet<String>();
		redirectUris.add(app.getRedirectUri().toString());
		for( String uri : app.getRedirectUris() )
			redirectUris.add(uri);
		if( !redirectUris.contains(redirectUri) )
			throw new InvalidUriException("redirect_uri '" + redirectUri + "' is not declared in the uris of application '" + clientId + "'");
		
		// Create authorization code
		AuthorizationCode authCode = this.authCodeSvc.createAuthorizationCode(
				app, 
				redirectUri, 
				scopes
		);
		
		// Send response
		AuthorizationCodeResponse ret = new AuthorizationCodeResponse();
		ret.setCode(authCode.getId());
		return ret;
	}
}
