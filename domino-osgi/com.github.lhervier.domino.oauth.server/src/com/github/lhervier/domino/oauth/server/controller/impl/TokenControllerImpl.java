package com.github.lhervier.domino.oauth.server.controller.impl;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.aop.ann.ctx.Oauth2DbContext;
import com.github.lhervier.domino.oauth.server.aop.ann.security.AppAuth;
import com.github.lhervier.domino.oauth.server.controller.TokenController;
import com.github.lhervier.domino.oauth.server.ex.BaseGrantException;
import com.github.lhervier.domino.oauth.server.ex.ForbiddenException;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.WrongPathException;
import com.github.lhervier.domino.oauth.server.ex.grant.GrantInvalidClientException;
import com.github.lhervier.domino.oauth.server.ex.grant.GrantInvalidRequestException;
import com.github.lhervier.domino.oauth.server.ex.grant.GrantUnsupportedGrantTypeException;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.services.AppService;
import com.github.lhervier.domino.oauth.server.services.GrantService;
import com.github.lhervier.domino.oauth.server.utils.Utils;

/**
 * Bean pour le endpoint "token"
 * @author Lionel HERVIER
 */
@Controller

@Oauth2DbContext				// Available at the oauth2.nsf db context only
@AppAuth						// Must be logged in as an application
public class TokenControllerImpl implements TokenController {

	/**
	 * The grant services
	 */
	@Autowired
	private Map<String, GrantService> grantServices;
	
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
	 * @see com.github.lhervier.domino.oauth.server.controller.TokenController#token(java.lang.String, java.lang.String)
	 */
	@RequestMapping(value = "/token", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> token(
			@RequestParam(value = "client_id", required = false) String clientId,
			@RequestParam(value = "grant_type", required = false) String grantType) throws NotAuthorizedException, ForbiddenException, WrongPathException, BaseGrantException, ServerErrorException {
		return this.token(this.tokenUser, clientId, grantType);
	}
	public Map<String, Object> token(
			NotesPrincipal user,
			String clientId,
			String grantType) throws BaseGrantException, ServerErrorException {
		// Extract application from current user (the application)
		// Must not be null as @AppAuth has already checked that the app exists
		Application app = this.appSvc.getApplicationFromName(user.getCommon()); 
		
		// Validate client_id : Must be the same as the client_id associated with the current user (application)
		// As the application is authenticated, the clientId is not mandatory.
		if( StringUtils.isEmpty(clientId) )
			clientId = app.getClientId();
		if( !Utils.equals(app.getClientId(), clientId) )
			throw new GrantInvalidClientException("invalid client_id : It does not correspond to the currently logged in application");
		
		// grant_type is mandatory
		if( StringUtils.isEmpty(grantType) )
			throw new GrantInvalidRequestException("grant_type is mandatory");
		
		// run grant
		GrantService svc = this.grantServices.get(grantType);
		if( svc == null )
			throw new GrantUnsupportedGrantTypeException("unknown grant_type '" + grantType + "'");
		return svc.createGrant(app);
	}
}
