package com.github.lhervier.domino.oauth.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public interface HttpContext {

	/**
	 * Return the request
	 */
	public HttpServletRequest getRequest();
	
	/**
	 * Return the response
	 */
	public HttpServletResponse getResponse();
	
	/**
	 * Return the session
	 */
	public HttpSession getSession();
}
