package com.github.lhervier.domino.oauth.client.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class StaticController {

	@RequestMapping(value = "/ngOauth2.js")
	public ModelAndView ngOauth2Js() {
		return new ModelAndView("ngOauth2.js");
	}
}
