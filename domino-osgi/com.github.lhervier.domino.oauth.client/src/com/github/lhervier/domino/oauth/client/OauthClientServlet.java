package com.github.lhervier.domino.oauth.client;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.github.lhervier.domino.spring.servlet.OsgiDispatcherServlet;

public class OauthClientServlet extends OsgiDispatcherServlet {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -7890653168372884954L;

	/**
	 * @see com.github.lhervier.domino.spring.servlet.OsgiDispatcherServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		System.out.println("Initializing OAUTH2 client servlet");
		super.init(config);
		System.out.println("OAUTH2 client servlet initialized");
	}

}
