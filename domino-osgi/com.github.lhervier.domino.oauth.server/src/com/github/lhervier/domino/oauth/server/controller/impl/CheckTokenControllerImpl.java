package com.github.lhervier.domino.oauth.server.controller.impl;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.aop.ann.ctx.Oauth2DbContext;
import com.github.lhervier.domino.oauth.server.aop.ann.security.AppAuth;
import com.github.lhervier.domino.oauth.server.controller.CheckTokenController;
import com.github.lhervier.domino.oauth.server.ex.ForbiddenException;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ex.WrongPathException;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.model.TokenContent;
import com.github.lhervier.domino.oauth.server.services.AppService;
import com.github.lhervier.domino.oauth.server.services.CheckTokenService;

/**
 * Token introspection endpoint;
 * See https://tools.ietf.org/html/rfc7662
 * @author Lionel HERVIER
 */
@Controller
@Oauth2DbContext			// Endpoint only available when accessing the oauth2.nsf database
@AppAuth					// Must be logged in as an application
public class CheckTokenControllerImpl implements CheckTokenController {

	/**
	 * Check token service
	 */
	@Autowired
	private CheckTokenService checkTokenSvc;
	
	/**
	 * App service
	 */
	@Autowired
	private AppService appSvc;
	
	/**
	 * We are unable to inject this bean as a method parameter
	 */
	@Autowired
	private NotesPrincipal checkTokenUser;
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.controller.CheckTokenController#checkToken(java.lang.String, javax.servlet.http.HttpServletResponse)
	 */
	@RequestMapping(value = "/checkToken", method = RequestMethod.POST)
	public @ResponseBody TokenContent checkToken(
			@RequestParam("token") String token,
			HttpServletResponse response) throws NotAuthorizedException, ForbiddenException, WrongPathException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		Application userApp = this.appSvc.getApplicationFromName(this.checkTokenUser.getCommon());		// Cannot be null because of @AppAuth on controller
		return this.checkTokenSvc.checkToken(userApp, token);
	}
}
