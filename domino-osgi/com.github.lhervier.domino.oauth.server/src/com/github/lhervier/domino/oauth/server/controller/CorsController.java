package com.github.lhervier.domino.oauth.server.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.github.lhervier.domino.oauth.server.aop.ann.ctx.Oauth2DbContext;
import com.github.lhervier.domino.oauth.server.aop.ann.ctx.ServerRootContext;

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
	 * @throws IOException
	 */
	@RequestMapping(value = "/userInfo")
    @ResponseStatus(HttpStatus.OK)
    @ServerRootContext
	public void handleUserInfoCors(HttpServletResponse response) {
       this.addCrosHeaders(response);
    }
	
	/**
	 * For CORS requests
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "/checkToken")
    @ResponseStatus(HttpStatus.OK)
    @Oauth2DbContext
	public void handleCheckTokenCors(HttpServletResponse response) throws IOException {
        this.addCrosHeaders(response);
    }
}
