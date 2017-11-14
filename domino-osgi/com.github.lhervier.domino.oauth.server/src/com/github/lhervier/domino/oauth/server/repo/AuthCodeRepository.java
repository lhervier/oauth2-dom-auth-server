package com.github.lhervier.domino.oauth.server.repo;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;

/**
 * Service to manage authorization codes
 * @author Lionel HERVIER
 */
public interface AuthCodeRepository {

	/**
	 * Return the oauth2 database as the server
	 * @param session the session to use to open the database
	 * @throws NotesException 
	 */
	public Database getOauth2Database(Session session) throws NotesException;
	
	/**
	 * Save an authorization code
	 * @param authCode the authorization code to save
	 */
	public AuthCodeEntity save(AuthCodeEntity authCode) throws NotesException;
	
	/**
	 * Loads an authorization code
	 * @param code the code
	 * @return the authorization code (or null if it does not exists)
	 */
	public AuthCodeEntity findOne(String code) throws NotesException;
	
	/**
	 * Remove an authorization code
	 * @param code the auth code
	 */
	public void delete(String code) throws NotesException;
}
