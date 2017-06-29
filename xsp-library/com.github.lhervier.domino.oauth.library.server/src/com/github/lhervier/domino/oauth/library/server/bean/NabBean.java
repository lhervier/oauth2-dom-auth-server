package com.github.lhervier.domino.oauth.library.server.bean;

import javax.servlet.http.HttpServletRequest;

import lotus.domino.Database;
import lotus.domino.NotesException;

import com.github.lhervier.domino.oauth.common.HttpContext;
import com.github.lhervier.domino.oauth.common.NotesContext;
import com.github.lhervier.domino.oauth.common.utils.DatabaseWrapper;

public class NabBean {

	/**
	 * The param bean
	 */
	private ParamsBean paramsBean;
	
	/**
	 * The notes context
	 */
	private NotesContext notesContext;
	
	/**
	 * The http context
	 */
	private HttpContext httpContext;
	
	/**
	 * @return the nab as configured in the parameters
	 * @throws NotesException
	 */
	public Database getNab() throws NotesException {
		HttpServletRequest request = this.httpContext.getRequest();
		String key = this.getClass().getName() + ".nab";
		if( request.getAttribute(key) != null )
			return (Database) request.getAttribute(key);
		
		DatabaseWrapper nab = new DatabaseWrapper(this.notesContext, this.paramsBean.getNab(), false);
		request.setAttribute(key, nab);
		
		return nab;
	}

	/**
	 * @return the nab as configured in the parameters
	 * @throws NotesException
	 */
	public synchronized Database getServerNab() throws NotesException {
		HttpServletRequest request = this.httpContext.getRequest();
		String key = this.getClass().getName() + ".serverNab";
		if( request.getAttribute(key) != null )
			return (Database) request.getAttribute(key);
		
		DatabaseWrapper nabAsSigner = new DatabaseWrapper(this.notesContext, this.paramsBean.getNab(), true);
		request.setAttribute(key, nabAsSigner);
		
		return nabAsSigner;
	}

	/**
	 * @param paramsBean the paramsBean to set
	 */
	public void setParamsBean(ParamsBean paramsBean) {
		this.paramsBean = paramsBean;
	}

	/**
	 * @param notesContext the notesContext to set
	 */
	public void setNotesContext(NotesContext notesContext) {
		this.notesContext = notesContext;
	}

	/**
	 * @param httpContext the httpContext to set
	 */
	public void setHttpContext(HttpContext httpContext) {
		this.httpContext = httpContext;
	}
}
