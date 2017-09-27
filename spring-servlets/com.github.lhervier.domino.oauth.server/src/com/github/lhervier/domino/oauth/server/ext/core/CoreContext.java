package com.github.lhervier.domino.oauth.server.ext.core;


/**
 * Context for core plugin
 * @author Lionel HERVIER
 */
public class CoreContext {

	/**
	 * The issuer
	 */
	private String iss;
	
	/**
	 * The audiance
	 */
	private String aud;
	
	/**
	 * The subject
	 */
	private String sub;
	
	public String getIss() {
		return iss;
	}

	public void setIss(String iss) {
		this.iss = iss;
	}

	public String getAud() {
		return aud;
	}

	public void setAud(String aud) {
		this.aud = aud;
	}

	public String getSub() {
		return sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}
}
