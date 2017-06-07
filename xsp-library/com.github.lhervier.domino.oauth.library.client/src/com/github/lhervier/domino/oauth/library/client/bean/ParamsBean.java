package com.github.lhervier.domino.oauth.library.client.bean;

import java.io.IOException;

import com.github.lhervier.domino.oauth.common.bean.BaseParamsBean;
import com.github.lhervier.domino.oauth.common.utils.JSFUtils;

public class ParamsBean extends BaseParamsBean {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -2672766621721522081L;
	
	/**
	 * Le chemin vers tomcat
	 */
	private String restServer;

	/**
	 * Envoi les paramètres en Json
	 * @throws IOException 
	 */
	public void param() throws IOException {
		JSFUtils.sendJson(this);
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
