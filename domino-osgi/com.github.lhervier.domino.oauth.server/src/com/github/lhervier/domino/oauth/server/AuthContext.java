package com.github.lhervier.domino.oauth.server;

import java.util.ArrayList;
import java.util.List;

import lotus.domino.Database;
import lotus.domino.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.spring.servlet.NotesContext;

/**
 * Encapsulation of the bearer and notes context
 * @author Lionel HERVIER
 */
@Component
public class AuthContext {

	/**
	 * The notes context
	 */
	@Autowired
	private NotesContext notesCtx;
	
	/**
	 * The bearer context
	 */
	@Autowired
	private BearerContext bearerCtx;
	
	/**
	 * Return the authentication type
	 */
	public boolean isBearerAuth() {
		return this.bearerCtx.isBearerAuth();
	}
	
	/**
	 * Return the authentication type
	 */
	public boolean isNotesAuth() {
		return !this.bearerCtx.isBearerAuth();
	}
	
	/**
	 * Return the user session
	 */
	public Session getUserSession() {
		if( this.bearerCtx.isBearerAuth() )
			return this.bearerCtx.getBearerSession();
		return this.notesCtx.getUserSession();
	}
	
	/**
	 * Return the server session
	 */
	public Session getServerSession() {
		return this.notesCtx.getServerSession();
	}
	
	/**
	 * Return the current user database as the user
	 */
	public Database getUserDatabase() {
		if( this.bearerCtx.isBearerAuth() )
			return null;
		return this.notesCtx.getUserDatabase();
	}
	
	/**
	 * Return the current database as the server
	 */
	public Database getServerDatabase() {
		return this.notesCtx.getServerDatabase();
	}
	
	/**
	 * Return the user roles
	 */
	public List<String> getRoles() {
		if( this.bearerCtx.isBearerAuth() )
			return new ArrayList<String>();
		return this.notesCtx.getUserRoles();
	}
	
	/**
	 * Return the scopes
	 */
	public List<String> getScopes() {
		if( !this.bearerCtx.isBearerAuth() )
			return new ArrayList<String>();
		return this.bearerCtx.getBearerScopes();
	}
	
	/**
	 * Return the client Id
	 */
	public String getClientId() {
		if( !this.bearerCtx.isBearerAuth() )
			return null;
		return this.bearerCtx.getBearerClientId();
	}
}
