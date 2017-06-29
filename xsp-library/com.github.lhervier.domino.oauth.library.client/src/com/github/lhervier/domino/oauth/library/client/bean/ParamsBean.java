package com.github.lhervier.domino.oauth.library.client.bean;

import java.io.IOException;

import com.github.lhervier.domino.oauth.common.HttpContext;
import com.github.lhervier.domino.oauth.common.bean.BaseParamsBean;
import com.github.lhervier.domino.oauth.common.utils.JSFUtils;

public class ParamsBean extends BaseParamsBean {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -2672766621721522081L;
	
	/**
	 * The http context
	 */
	private HttpContext httpContext;
	
	/**
	 * Le chemin vers tomcat
	 */
	private String restServer;

	/**
	 * Constructor
	 */
	public ParamsBean() {
		super("Params", "PARAM_");
	}
	
	/**
	 * Envoi les paramètres en Json
	 * @throws IOException 
	 */
	public void param() throws IOException {
		JSFUtils.sendJson(this.httpContext.getResponse(), this);
	}
	
	/**
	 * @param httpContext the httpContext to set
	 */
	public void setHttpContext(HttpContext httpContext) {
		this.httpContext = httpContext;
	}

	// =================================================================================
	
	/**
	 * @return the restServer
	 */
	public String getRestServer() {
		return restServer;
	}

	/**
	 * @param restServer the restServer to set
	 */
	public void setRestServer(String restServer) {
		this.restServer = restServer;
	}
}
