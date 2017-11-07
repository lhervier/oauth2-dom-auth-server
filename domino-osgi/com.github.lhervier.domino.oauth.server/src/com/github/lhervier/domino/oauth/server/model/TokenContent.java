package com.github.lhervier.domino.oauth.server.model;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * The content of a token
 * 
 * @author Lionel HERVIER
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TokenContent {

	/**
	 * Active or not
	 */
	private boolean active;
	
	/**
	 * Scopes
	 */
	private String scope;
	
	/**
	 * The client id
	 */
	@JsonProperty("client_id")
	private String clientId;
	
	/**
	 * The associated username
	 */
	private String username;
	
	/**
	 * The associated username
	 */
	@JsonProperty("user_name")
	private String springUsername;
	
	/**
	 * The token type
	 */
	@JsonProperty("token_type")
	private String tokenType;
	
	/**
	 * Expiration date
	 */
	private long exp;
	
	/**
	 * Issued at
	 */
	private long iat;
	
	/**
	 * ???
	 */
	private long nbf;
	
	/**
	 * The subject
	 */
	private String sub;
	
	/**
	 * Audience
	 */
	private String aud;
	
	/**
	 * Issuer
	 */
	private String iss;

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public long getExp() {
		return exp;
	}

	public void setExp(long exp) {
		this.exp = exp;
	}

	public long getIat() {
		return iat;
	}

	public void setIat(long iat) {
		this.iat = iat;
	}

	public long getNbf() {
		return nbf;
	}

	public void setNbf(long nbf) {
		this.nbf = nbf;
	}

	public String getSpringUsername() {
		return springUsername;
	}

	public void setSpringUsername(String springUsername) {
		this.springUsername = springUsername;
	}

	public String getSub() {
		return sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}

	public String getAud() {
		return aud;
	}

	public void setAud(String aud) {
		this.aud = aud;
	}

	public String getIss() {
		return iss;
	}

	public void setIss(String iss) {
		this.iss = iss;
	}
}
