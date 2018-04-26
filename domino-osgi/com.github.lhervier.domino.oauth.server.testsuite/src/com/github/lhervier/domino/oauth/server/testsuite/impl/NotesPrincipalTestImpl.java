package com.github.lhervier.domino.oauth.server.testsuite.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;

@Component
@Primary
public class NotesPrincipalTestImpl implements NotesPrincipal {

	private AuthType authType;
	private String clientId;
	private String common;
	private String currentDatabasePath;
	private List<String> roles = new ArrayList<String>();
	private List<String> scopes = new ArrayList<String>();
	private String name;
	
	@Override
	public AuthType getAuthType() {
		return this.authType;
	}

	@Override
	public String getClientId() {
		return this.clientId;
	}

	@Override
	public String getCommon() {
		return this.common;
	}

	@Override
	public String getCurrentDatabasePath() {
		return this.currentDatabasePath;
	}

	@Override
	public List<String> getRoles() {
		return this.roles;
	}

	@Override
	public List<String> getScopes() {
		return this.scopes;
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	// ========================================================================

	/**
	 * @param authType the authType to set
	 */
	public void setAuthType(AuthType authType) {
		this.authType = authType;
	}

	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * @param common the common to set
	 */
	public void setCommon(String common) {
		this.common = common;
	}

	/**
	 * @param currentDatabasePath the currentDatabasePath to set
	 */
	public void setCurrentDatabasePath(String currentDatabasePath) {
		this.currentDatabasePath = currentDatabasePath;
	}

	/**
	 * @param roles the roles to set
	 */
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	/**
	 * @param scopes the scopes to set
	 */
	public void setScopes(List<String> scopes) {
		this.scopes = scopes;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}
