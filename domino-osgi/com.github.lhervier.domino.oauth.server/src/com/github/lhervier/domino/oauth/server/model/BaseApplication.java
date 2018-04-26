package com.github.lhervier.domino.oauth.server.model;

/**
 * Minimum information about an application
 * @author Lionel HERVIER
 */
public class BaseApplication {

	/**
	 * Son nom
	 */
	private String name;
	
	/**
	 * Son client_id
	 */
	private String clientId;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
}
