package com.github.lhervier.domino.oauth.client.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.lhervier.domino.oauth.client.Constants;
import com.github.lhervier.domino.oauth.client.model.TokenResponse;

@Controller
public class TokenController {

	/**
	 * The http session
	 */
	@Autowired
	private HttpSession httpSession;
	
	/**
	 * The spring environment
	 */
	@Autowired
	private Environment env;
	
	/**
	 * Send the access token
	 */
	@RequestMapping(value = "/accesstoken", method = RequestMethod.GET)
	public @ResponseBody TokenResponse accessToken() {
		TokenResponse resp = new TokenResponse();
		resp.setAccessToken((String) this.httpSession.getAttribute(Constants.SESSION_ACCESS_TOKEN));
		resp.setIdToken((String) this.httpSession.getAttribute(Constants.SESSION_ID_TOKEN));
		resp.setUserInfoEndpoint(this.env.getProperty("oauth2.client.endpoints.userInfo"));
		return resp;
	}
}
