package com.github.lhervier.domino.oauth.server;

import java.util.List;

import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;

public class AuthCodeNotesPrincipal implements NotesPrincipal {

	/**
	 * The auth code
	 */
	private AuthCodeEntity entity;
	
	/**
	 * Constructor
	 */
	public AuthCodeNotesPrincipal(AuthCodeEntity entity) {
		this.entity = entity;
	}
	
	/**
	 * @see java.security.Principal#getName()
	 */
	@Override
	public String getName() {
		return this.entity.getFullName();
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.NotesPrincipal#getCommon()
	 */
	@Override
	public String getCommon() {
		return this.entity.getCommonName();
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.NotesPrincipal#getAuthType()
	 */
	@Override
	public AuthType getAuthType() {
		return AuthType.valueOf(this.entity.getAuthType());
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.NotesPrincipal#getScopes()
	 */
	@Override
	public List<String> getScopes() {
		return this.entity.getScopes();
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.NotesPrincipal#getClientId()
	 */
	@Override
	public String getClientId() {
		return this.entity.getClientId();
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.NotesPrincipal#getRoles()
	 */
	@Override
	public List<String> getRoles() {
		return this.entity.getRoles();
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.NotesPrincipal#getCurrentDatabasePath()
	 */
	@Override
	public String getCurrentDatabasePath() {
		return this.entity.getDatabasePath();
	}

	
}
