package com.github.lhervier.domino.oauth.common.ctx;

import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

import com.github.lhervier.domino.oauth.common.NotesContext;
import com.github.lhervier.domino.oauth.common.utils.JSFUtils;

public class JSFNotesContext implements NotesContext {

	/**
	 * @see com.github.lhervier.domino.oauth.common.NotesContext#getServerDatabase()
	 */
	@Override
	public synchronized Database getServerDatabase() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) ctx.getExternalContext().getRequest();
		String key = this.getClass().getName() + ".serverDatabase";
		if( request.getAttribute(key) != null )
			return (Database) request.getAttribute(key);
		
		JSFDatabaseWrapper dbAsSigner = new JSFDatabaseWrapper();
		dbAsSigner.setAsSigner(true);
		try {
			dbAsSigner.setFilePath(JSFUtils.getDatabase().getFilePath());
		} catch (NotesException e) {
			throw new RuntimeException(e);
		}
		request.setAttribute(key, dbAsSigner);
		
		return dbAsSigner;
	}

	/**
	 * @see com.github.lhervier.domino.oauth.common.NotesContext#getServerSession()
	 */
	@Override
	public Session getServerSession() {
		return JSFUtils.getSessionAsSigner();
	}

	/**
	 * @see com.github.lhervier.domino.oauth.common.NotesContext#getUserDatabase()
	 */
	@Override
	public Database getUserDatabase() {
		return JSFUtils.getDatabase();
	}

	/**
	 * @see com.github.lhervier.domino.oauth.common.NotesContext#getUserRoles()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getUserRoles() {
		return JSFUtils.getContext().getUser().getRoles();
	}

	/**
	 * @see com.github.lhervier.domino.oauth.common.NotesContext#getUserSession()
	 */
	@Override
	public Session getUserSession() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		return (Session) ctx.getApplication().getVariableResolver().resolveVariable(ctx, "session");
	}

}
