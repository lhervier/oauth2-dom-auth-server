package com.github.lhervier.domino.oauth.server.ext.core;

import org.codehaus.jackson.annotate.JsonProperty;

import com.github.lhervier.domino.oauth.server.IExpirable;

/**
 * Representation of the access token
 * @author Lionel HERVIER
 */
public class AccessToken implements IExpirable {

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
	
	/**
	 * Expiration
	 */
	@JsonProperty("exp")
	private long expires;
	
	/**
	 * The scopes
	 */
	private String scope;

	public long getExpires() {
		return expires;
	}

	public void setExpires(long exp) {
		this.expires = exp;
	}

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

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getSub() {
		return sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}
}
