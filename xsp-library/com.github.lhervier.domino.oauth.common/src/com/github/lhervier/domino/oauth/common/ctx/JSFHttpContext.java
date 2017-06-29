package com.github.lhervier.domino.oauth.common.ctx;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.github.lhervier.domino.oauth.common.HttpContext;

public class JSFHttpContext implements HttpContext {

	/**
	 * @see com.github.lhervier.domino.oauth.common.HttpContext#getRequest()
	 */
	@Override
	public HttpServletRequest getRequest() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		return (HttpServletRequest) ctx.getExternalContext().getRequest();
	}

	/**
	 * @see com.github.lhervier.domino.oauth.common.HttpContext#getResponse()
	 */
	@Override
	public HttpServletResponse getResponse() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		return (HttpServletResponse) ctx.getExternalContext().getResponse();
	}

	/**
	 * @see com.github.lhervier.domino.oauth.common.HttpContext#getSession()
	 */
	@Override
	public HttpSession getSession() {
		return this.getRequest().getSession();
	}
}
