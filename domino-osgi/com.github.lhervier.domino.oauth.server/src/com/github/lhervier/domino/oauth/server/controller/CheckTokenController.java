package com.github.lhervier.domino.oauth.server.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import lotus.domino.NotesException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.aop.ann.ctx.Oauth2DbContext;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.model.TokenContent;
import com.github.lhervier.domino.oauth.server.services.CheckTokenService;

/**
 * Token introspection endpoint;
 * See https://tools.ietf.org/html/rfc7662
 * @author Lionel HERVIER
 */
@Controller
public class CheckTokenController {

	/**
	 * Check token service
	 */
	@Autowired
	private CheckTokenService checkTokenSvc;
	
	/**
	 * For CORS requests
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "/checkToken", method = RequestMethod.OPTIONS)
    @ResponseStatus(HttpStatus.OK)
	@Oauth2DbContext
	public void handleCors(HttpServletResponse response) throws IOException {
        response.addHeader("Access-Control-Allow-Headers", "authorization");
        response.addHeader("Access-Control-Max-Age", "60"); // seconds to cache preflight request --> less OPTIONS traffic
        response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.addHeader("Access-Control-Allow-Origin", "*");
    }
	
	// =======================================================================================
	
	/**
	 * We are unable to inject this bean as a method parameter
	 */
	@Autowired
	private NotesPrincipal checkTokenUser;
	
	/**
	 * Check a given token
	 * @param token the token
	 * @return the token content
	 * @throws IOException
	 * @throws NotesException
	 * @throws NotAuthorizedException
	 */
	@RequestMapping(value = "/checkToken", method = RequestMethod.POST)
	@Oauth2DbContext
	public @ResponseBody TokenContent checkToken(
			@RequestParam("token") String token,
			HttpServletResponse response) throws IOException, NotesException, NotAuthorizedException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		return this.checkTokenSvc.checkToken(this.checkTokenUser, token);
	}
}
