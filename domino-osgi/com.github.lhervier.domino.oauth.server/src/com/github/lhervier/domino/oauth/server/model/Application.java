package com.github.lhervier.domino.oauth.server.model;

import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;

/**
 * Une application OAUTH2
 * @author Lionel HERVIER
 */
public class Application extends ApplicationEntity {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 5374108086016550299L;
	
	/**
	 * Application full name
	 */
	private String fullName;

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
	
}
