package com.github.lhervier.domino.oauth.server.repo;

import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;

/**
 * Service to manage authorization codes
 * @author Lionel HERVIER
 */
public interface AuthCodeRepository {

	/**
	 * Save an authorization code
	 * @param authCode the authorization code to save
	 */
	public AuthCodeEntity save(AuthCodeEntity authCode);
	
	/**
	 * Loads an authorization code
	 * @param code the code
	 * @return the authorization code (or null if it does not exists)
	 */
	public AuthCodeEntity findOne(String code);
	
	/**
	 * Remove an authorization code
	 * @param code the auth code
	 */
	public boolean delete(String code);
}
