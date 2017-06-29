package com.github.lhervier.domino.oauth.library.server.ctx;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import lotus.domino.Database;
import lotus.domino.NotesException;

import com.github.lhervier.domino.oauth.common.ctx.JSFDatabaseWrapper;
import com.github.lhervier.domino.oauth.library.server.ServerContext;
import com.github.lhervier.domino.oauth.library.server.bean.ParamsBean;

public class JSFServerContext implements ServerContext {

	/**
	 * The param bean
	 */
	private ParamsBean paramsBean;
	
	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ServerContext#getNab()
	 */
	@Override
	public Database getNab() throws NotesException {
		FacesContext ctx = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) ctx.getExternalContext().getRequest();
		String key = this.getClass().getName() + ".nab";
		if( request.getAttribute(key) != null )
			return (Database) request.getAttribute(key);
		
		JSFDatabaseWrapper nab = new JSFDatabaseWrapper(this.paramsBean.getNab(), false);
		request.setAttribute(key, nab);
		
		return nab;
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ServerContext#getServerNab()
	 */
	@Override
	public synchronized Database getServerNab() throws NotesException {
		FacesContext ctx = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) ctx.getExternalContext().getRequest();
		String key = this.getClass().getName() + ".serverNab";
		if( request.getAttribute(key) != null )
			return (Database) request.getAttribute(key);
		
		JSFDatabaseWrapper nabAsSigner = new JSFDatabaseWrapper(this.paramsBean.getNab(), true);
		request.setAttribute(key, nabAsSigner);
		
		return nabAsSigner;
	}

	/**
	 * @param paramsBean the paramsBean to set
	 */
	public void setParamsBean(ParamsBean paramsBean) {
		this.paramsBean = paramsBean;
	}
}
