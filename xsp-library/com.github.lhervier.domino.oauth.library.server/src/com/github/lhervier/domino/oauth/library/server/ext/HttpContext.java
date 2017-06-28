package com.github.lhervier.domino.oauth.library.server.ext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;

@Component
public class HttpContext {

	/**
	 * The http servlet request
	 */
	private ThreadLocal<HttpServletRequest> request = new ThreadLocal<HttpServletRequest>();
	
	/**
	 * The http servlet response
	 */
	private ThreadLocal<HttpServletResponse> response = new ThreadLocal<HttpServletResponse>();
	
	/**
	 * The http session
	 */
	private ThreadLocal<HttpSession> session = new ThreadLocal<HttpSession>();
	
	/**
	 * Initialisation
	 */
	public void init(HttpServletRequest request, HttpServletResponse response) {
		this.request.set(request);
		this.response.set(response);
		this.session.set(request.getSession());
	}
	
	/**
	 * Cleanup
	 */
	public void cleanUp() {
		this.request.set(null);
		this.response.set(null);
		this.session.set(null);
	}
	
	/**
	 * Return the request
	 */
	public HttpServletRequest getRequest() {
		return this.request.get();
	}
	
	/**
	 * Return the response
	 */
	public HttpServletResponse getResponse() {
		return this.response.get();
	}
	
	/**
	 * Return the session
	 */
	public HttpSession getSession() {
		return this.session.get();
	}
}
