package com.github.lhervier.domino.oauth.server;

import java.security.Principal;
import java.util.List;

public interface NotesPrincipal extends Principal {

	/**
	 * Return the user common name
	 * @return the user common name
	 */
	public String getCommon();
	
	/**
	 * Is the current user authenticated by notes ?
	 */
	public boolean isNotesAuth();
	
	/**
	 * Is the current user authenticated using a bearer token
	 */
	public boolean isBearerAuth();
	
	/**
	 * Return the bearer scopes
	 */
	public List<String> getScopes();
	
	/**
	 * Return the client Id
	 */
	public String getClientId();
}
