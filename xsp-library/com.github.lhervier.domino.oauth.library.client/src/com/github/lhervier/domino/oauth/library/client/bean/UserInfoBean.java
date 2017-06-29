package com.github.lhervier.domino.oauth.library.client.bean;

import java.io.IOException;

import com.github.lhervier.domino.oauth.common.HttpContext;
import com.github.lhervier.domino.oauth.common.utils.HttpUtils;

public class UserInfoBean {

	/**
	 * The http context
	 */
	private HttpContext httpContext;
	
	/**
	 * Retourne les infos utilisateur
	 * @throws IOException 
	 */
	public void userInfo() throws IOException {
		HttpUtils.sendJson(this.httpContext.getResponse(), this.httpContext.getSession().getAttribute("id_token"));
	}

	/**
	 * @param httpContext the httpContext to set
	 */
	public void setHttpContext(HttpContext httpContext) {
		this.httpContext = httpContext;
	}
}
