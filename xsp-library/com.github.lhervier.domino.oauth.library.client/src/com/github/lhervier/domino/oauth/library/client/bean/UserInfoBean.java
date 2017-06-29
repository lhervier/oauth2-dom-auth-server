package com.github.lhervier.domino.oauth.library.client.bean;

import java.io.IOException;

import com.github.lhervier.domino.oauth.common.HttpContext;
import com.github.lhervier.domino.oauth.common.utils.JSFUtils;

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
		JSFUtils.sendJson(this.httpContext.getResponse(), JSFUtils.getSessionScope().get("id_token"));
	}

	/**
	 * @param httpContext the httpContext to set
	 */
	public void setHttpContext(HttpContext httpContext) {
		this.httpContext = httpContext;
	}
}
