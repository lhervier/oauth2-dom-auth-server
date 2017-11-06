package com.github.lhervier.domino.oauth.server;

import java.security.Principal;
import java.util.List;

import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.utils.DominoUtils;
import com.github.lhervier.domino.spring.servlet.NotesContext;

/**
 * @author Lionel HERVIER
 */
@Component
public class NotesUserPrincipal implements Principal {

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
	 * Return the name of the principal
	 */
	public String getName() {
		Session session;
		if( bearerCtx.getBearerSession() != null )
			session = bearerCtx.getBearerSession();
		else if( notesCtx.getUserSession() != null )
			session = notesCtx.getUserSession();
		else
			return null;
		
		try {
			return session.getEffectiveUserName();
		} catch (NotesException e) {
			return null;
		}
	}
	
	/**
	 * Return the common name of the user
	 * @return
	 */
	public String getCommon() {
		if( this.getName() == null )
			return null;
		
		Name nn = null;
		try {
			nn = notesCtx.getServerSession().createName(this.getName());
			return nn.getCommon();
		} catch (NotesException e) {
			return null;
		} finally {
			DominoUtils.recycleQuietly(nn);
		}
	}
	
	/**
	 * Is the current user authenticated by notes ?
	 */
	public boolean isNotesAuth() {
		return this.bearerCtx.getBearerSession() == null;
	}
	
	/**
	 * Is the current user authenticated using a bearer token
	 */
	public boolean isBearerAuth() {
		return !this.isNotesAuth();
	}
	
	/**
	 * Return the bearer scopes
	 */
	public List<String> getScopes() {
		return this.bearerCtx.getBearerScopes();
	}
	
	/**
	 * Return the client Id
	 */
	public String getClientId() {
		return this.bearerCtx.getBearerClientId();
	}
}
