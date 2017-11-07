package com.github.lhervier.domino.oauth.server.controller;

import java.util.Map;

import lotus.domino.NotesException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.aop.ann.ctx.Oauth2DbContext;
import com.github.lhervier.domino.oauth.server.ex.GrantException;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.grant.InvalidClientException;
import com.github.lhervier.domino.oauth.server.ex.grant.InvalidRequestException;
import com.github.lhervier.domino.oauth.server.ex.grant.UnsupportedGrantTypeException;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.services.AppService;
import com.github.lhervier.domino.oauth.server.services.BaseGrantService;

/**
 * Bean pour le endpoint "token"
 * @author Lionel HERVIER
 */
@Controller
@Oauth2DbContext
public class TokenController {

	/**
	 * The grant services
	 */
	@Autowired
	private Map<String, BaseGrantService> grantServices;
	
	/**
	 * Application service
	 */
	@Autowired
	private AppService appSvc;
	
	/**
	 * We are note able to inject this bean as a method argument
	 */
	@Autowired
	private NotesPrincipal tokenUser;
	
	/**
	 * Generate a token
	 * @throws GrantException error that must be serialized to the user
	 * @throws ServerErrorException main error
	 * @throws NotesException 
	 */
	@RequestMapping(value = "/token", method = RequestMethod.POST)
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
			NotesPrincipal user,
			String clientId,
			String grantType,
			String code,
			String scope,
			String refreshToken,
			String redirectUri) throws GrantException, ServerErrorException, NotesException {
		// Extract application from current user (the application)
		Application app = this.appSvc.getApplicationFromName(user.getCommon());
		if( app == null )
			throw new InvalidClientException("Current user do not correspond to a declared application");
		
		// Validate client_id : Must be the same as the client_id associated with the current user (application)
		// As the application is authenticated, the clientId is not mandatory.
		if( StringUtils.isEmpty(clientId) )
			clientId = app.getClientId();
		if( !app.getClientId().equals(clientId) )
			throw new InvalidClientException("client_id do not correspond to the currently logged in application");
		
		// grant_type is mandatory
		if( StringUtils.isEmpty(grantType) )
			throw new InvalidRequestException();
		
		// run grant
		BaseGrantService svc = this.grantServices.get(grantType);
		if( svc == null )
			throw new UnsupportedGrantTypeException("grant_type '" + grantType + "' is not supported");
		return svc.createGrant(app, grantType, code, scope, refreshToken, redirectUri);
	}
}
