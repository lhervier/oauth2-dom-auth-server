package com.github.lhervier.domino.oauth.library.client.bean;

import java.io.IOException;

import lotus.domino.NotesException;

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
	 * Constructor
	 * @throws NotesException 
	 */
	public ParamsBean() throws NotesException {
		this.setViewName("Params");
		this.setPrefix("PARAM_");
	}
	
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
