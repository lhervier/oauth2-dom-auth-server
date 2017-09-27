package com.github.lhervier.domino.oauth.server.controller;

import java.io.IOException;

import lotus.domino.NotesException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.github.lhervier.domino.oauth.server.aop.ann.Oauth2DbContext;
import com.github.lhervier.domino.oauth.server.aop.ann.Roles;
import com.github.lhervier.domino.oauth.server.services.SecretService;

@Controller
@RequestMapping("/html")
public class SecretController {

	/**
	 * The secret service
	 */
	@Autowired
	private SecretService secretSvc;
	
	/**
	 * Ltpa config name
	 */
	@Value("${oauth2.server.core.signKey}")
	private String coreSignKey;
	
	/**
	 * Display the secret
	 * @throws IOException 
	 * @throws NotesException 
	 */
	@RequestMapping("/secret")
	@Oauth2DbContext
	@Roles(roles = {"SecretExtractor"})
	public ModelAndView extractSecret() throws NotesException, IOException {
		ModelAndView ret = new ModelAndView("secret");
		ret.addObject("secret", this.secretSvc.getSignSecretBase64(this.coreSignKey));
		return ret;
	}
}
