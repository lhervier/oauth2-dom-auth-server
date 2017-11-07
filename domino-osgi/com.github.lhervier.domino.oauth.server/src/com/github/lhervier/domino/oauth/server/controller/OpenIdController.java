package com.github.lhervier.domino.oauth.server.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import lotus.domino.NotesException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.aop.ann.ServerRootContext;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ext.openid.IdToken;
import com.github.lhervier.domino.oauth.server.services.OpenIdUserInfoService;

/**
 * Controller to manage userInfo openId end point
 * @author Lionel HERVIER
 */
@Controller
public class OpenIdController {

	/**
	 * The opend id service
	 */
	@Autowired
	private OpenIdUserInfoService userInfoSvc;
	
	/**
	 * For CORS requests
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "/userInfo", method = RequestMethod.OPTIONS)
    @ResponseStatus(HttpStatus.OK)
	@ServerRootContext
	public void handleCors(HttpServletResponse response) throws IOException {
        response.addHeader("Access-Control-Allow-Headers", "authorization");
        response.addHeader("Access-Control-Max-Age", "60"); // seconds to cache preflight request --> less OPTIONS traffic
        response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.addHeader("Access-Control-Allow-Origin", "*");
    }
	
	// ======================================================================
	
	/**
	 * We are unable to inject this bean as a method parameter
	 */
	@Autowired
	private NotesPrincipal userInfoUser;
	
	/**
	 * The userInfo end point
	 * @return
	 * @throws NotesException 
	 */
	@RequestMapping(value = "/userInfo", method = RequestMethod.GET)
	@ServerRootContext
	public @ResponseBody IdToken userInfo(HttpServletResponse response) throws NotesException, NotAuthorizedException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		return this.userInfoSvc.userInfo(this.userInfoUser);
	}
}
