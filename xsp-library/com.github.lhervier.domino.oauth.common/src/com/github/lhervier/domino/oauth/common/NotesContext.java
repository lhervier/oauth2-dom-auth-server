package com.github.lhervier.domino.oauth.common;

import java.util.List;

import lotus.domino.Database;
import lotus.domino.Session;

public interface NotesContext {

	/**
	 * Return the user session
	 */
	public Session getUserSession();
	
	/**
	 * Return the server session
	 */
	public Session getServerSession();
	
	/**
	 * Return the current database with the user rights
	 */
	public Database getUserDatabase();
	
	/**
	 * Return the current database opened with the server session
	 */
	public Database getServerDatabase();
	
	/**
	 * Return the current user roles
	 */
	public List<String> getUserRoles();
}
