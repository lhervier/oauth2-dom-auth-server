package com.github.lhervier.domino.oauth.server.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Une application OAUTH2
 * @author Lionel HERVIER
 */
public class Application extends BaseApplication {

	/**
	 * Application full name
	 */
	private String fullName;
	
	/**
	 * Client type
	 */
	private ClientType clientType = ClientType.PUBLIC;
	
	/**
	 * Son URI de redirection par défaut
	 */
	private String redirectUri;
	
	/**
	 * Ses autres URIs de redirection
	 */
	private List<String> redirectUris = new ArrayList<String>();
	
	/**
	 * Les personnes autorisées à se logger sur cette application
	 */
	private String readers;
	
	/**
	 * @return the readers
	 */
	public String getReaders() {
		return readers;
	}

	/**
	 * @param readers the readers to set
	 */
	public void setReaders(String readers) {
		this.readers = readers;
	}

	/**
	 * @return the redirectUri
	 */
	public String getRedirectUri() {
		return redirectUri;
	}

	/**
	 * @param redirectUrl the redirectUri to set
	 */
	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	/**
	 * @return the redirectUris
	 */
	public List<String> getRedirectUris() {
		return redirectUris;
	}

	/**
	 * @param redirectUris the redirectUris to set
	 */
	public void setRedirectUris(List<String> redirectUris) {
		this.redirectUris = redirectUris;
	}

	/**
	 * @return the fullName
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * @param fullName the fullName to set
	 */
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	/**
	 * @return the clientType
	 */
	public ClientType getClientType() {
		return clientType;
	}

	/**
	 * @param clientType the clientType to set
	 */
	public void setClientType(ClientType clientType) {
		this.clientType = clientType;
	}
}
