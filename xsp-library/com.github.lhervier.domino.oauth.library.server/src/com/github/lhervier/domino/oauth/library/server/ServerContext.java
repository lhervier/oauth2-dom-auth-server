package com.github.lhervier.domino.oauth.library.server;

import lotus.domino.Database;
import lotus.domino.NotesException;

public interface ServerContext {

	/**
	 * Return the NAB
	 */
	public Database getNab() throws NotesException;
	
	/**
	 * Return the NAB as the server
	 */
	public Database getServerNab() throws NotesException;
}
