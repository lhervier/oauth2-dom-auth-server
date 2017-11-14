package com.github.lhervier.domino.oauth.server.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.github.lhervier.domino.oauth.server.aop.ann.ctx.Oauth2DbContext;
import com.github.lhervier.domino.oauth.server.aop.ann.ctx.ServerRootContext;
import com.github.lhervier.domino.oauth.server.ex.ForbiddenException;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ex.WrongPathException;

/**
 * To handle cors requests
 * @author Lionel HERVIER
 */
@Controller
@RequestMapping(method = RequestMethod.OPTIONS)
public class CorsController {

	/**
	 * Add cors headers
	 */
	private void addCrosHeaders(HttpServletResponse response) {
		response.addHeader("Access-Control-Allow-Headers", "authorization");
        response.addHeader("Access-Control-Max-Age", "60"); // seconds to cache preflight request --> less OPTIONS traffic
        response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.addHeader("Access-Control-Allow-Origin", "*");
	}
	
	/**
	 * For CORS requests for the /userInfo endpoint
	 * @param response
	 */
	@RequestMapping(value = "/userInfo")
    @ResponseStatus(HttpStatus.OK)
    @ServerRootContext					// userInfo endpoint is available at the server root context only
	public void handleUserInfoCors(HttpServletResponse response) throws NotAuthorizedException, ForbiddenException, WrongPathException {
       this.addCrosHeaders(response);
    }
	
	/**
	 * For CORS requests
	 * @param response
	 */
	@RequestMapping(value = "/checkToken")
    @ResponseStatus(HttpStatus.OK)
    @Oauth2DbContext					// checkToken is available at the oauth2.nsf db context only
	public void handleCheckTokenCors(HttpServletResponse response) throws NotAuthorizedException, ForbiddenException, WrongPathException {
        this.addCrosHeaders(response);
    }
}
