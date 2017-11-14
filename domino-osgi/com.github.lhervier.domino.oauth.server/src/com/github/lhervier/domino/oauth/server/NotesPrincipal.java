package com.github.lhervier.domino.oauth.server;

import java.security.Principal;
import java.util.List;

public interface NotesPrincipal extends Principal {

	public static enum AuthType {
		NOTES, BEARER
	}
	
	/**
	 * Return the user common name
	 * @return the user common name
	 */
	public String getCommon();
	
	/**
	 * Current authentication method
	 */
	public AuthType getAuthType();
	
	/**
	 * Return the bearer scopes
	 */
	public List<String> getScopes();
	
	/**
	 * Return the client Id
	 */
	public String getClientId();
	
	/**
	 * Return the roles
	 */
	public List<String> getRoles();
	
	/**
	 * Return the path to the current database
	 */
	public String getCurrentDatabasePath();

}
