package com.github.lhervier.domino.oauth.server.model;

import java.util.List;

/**
 * An authorize request
 * @author Lionel HERVIER
 */
public class AuthorizeRequest {

	/**
	 * The response type
	 */
	private List<String> responseTypes;
	
	/**
	 * The client Id
	 */
	private String clientId;
	
	/**
	 * The scopes
	 */
	private List<String> scopes;
	
	/**
	 * The state
	 */
	private String state;
	
	/**
	 * The redirect uri
	 */
	private String redirectUri;

	public List<String> getResponseTypes() { return responseTypes; }
	public void setResponseTypes(List<String> responseTypes) { this.responseTypes = responseTypes; }
	public String getClientId() { return clientId; }
	public void setClientId(String clientId) { this.clientId = clientId; }
	public List<String> getScopes() { return scopes; }
	public void setScopes(List<String> scopes) { this.scopes = scopes; }
	public String getState() { return state; }
	public void setState(String state) { this.state = state; }
	public String getRedirectUri() { return redirectUri; }
	public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
}
