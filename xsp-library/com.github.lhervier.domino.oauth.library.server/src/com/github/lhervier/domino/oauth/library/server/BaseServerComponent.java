package com.github.lhervier.domino.oauth.library.server;

import lotus.domino.Database;
import lotus.domino.NotesException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.github.lhervier.domino.spring.servlet.ServerSession;
import com.github.lhervier.domino.spring.servlet.UserSession;

public class BaseServerComponent {

	/**
	 * The session as the server
	 */
	@Autowired
	private ServerSession serverSession;
	
	/**
	 * The session as the current user
	 */
	@Autowired
	private UserSession userSession;
	
	/**
	 * The database where to store application information
	 */
	@Value("${oauth2.server.db}")
	private String oauth2db;
	
	/**
	 * Return the oauth2 database as the server
	 * @throws NotesException 
	 */
	public Database getOauth2DatabaseAsServer() throws NotesException {
		return DominoUtils.openDatabase(this.serverSession, this.oauth2db);
	}
	
	/**
	 * Return the oauth2 database as the user (the application)
	 * @throws NotesException 
	 */
	public Database getOauth2DatabaseAsUser() throws NotesException {
		return DominoUtils.openDatabase(this.userSession, this.oauth2db);
	}
	
	
}
