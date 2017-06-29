package com.github.lhervier.domino.oauth.library.server.ext.wrap;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import com.github.lhervier.domino.oauth.library.server.ext.ctx.HttpContext;

@SuppressWarnings("deprecation")
public class WrappedHttpSession implements HttpSession {

	private HttpContext httpContext;

	public WrappedHttpSession(HttpContext httpCtx) {
		this.httpContext = httpCtx;
	}
	
	/**
	 * @param paramString
	 * @return
	 * @see javax.servlet.http.HttpSession#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String paramString) {
		return this.httpContext.getSession().getAttribute(paramString);
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpSession#getAttributeNames()
	 */
	@SuppressWarnings("unchecked")
	public Enumeration getAttributeNames() {
		return this.httpContext.getSession().getAttributeNames();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpSession#getCreationTime()
	 */
	public long getCreationTime() {
		return this.httpContext.getSession().getCreationTime();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpSession#getId()
	 */
	public String getId() {
		return this.httpContext.getSession().getId();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpSession#getLastAccessedTime()
	 */
	public long getLastAccessedTime() {
		return this.httpContext.getSession().getLastAccessedTime();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpSession#getMaxInactiveInterval()
	 */
	public int getMaxInactiveInterval() {
		return this.httpContext.getSession().getMaxInactiveInterval();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpSession#getServletContext()
	 */
	public ServletContext getServletContext() {
		return this.httpContext.getSession().getServletContext();
	}

	/**
	 * @return
	 * @deprecated
	 * @see javax.servlet.http.HttpSession#getSessionContext()
	 */
	public HttpSessionContext getSessionContext() {
		return this.httpContext.getSession().getSessionContext();
	}

	/**
	 * @param paramString
	 * @return
	 * @deprecated
	 * @see javax.servlet.http.HttpSession#getValue(java.lang.String)
	 */
	public Object getValue(String paramString) {
		return this.httpContext.getSession().getValue(paramString);
	}

	/**
	 * @return
	 * @deprecated
	 * @see javax.servlet.http.HttpSession#getValueNames()
	 */
	public String[] getValueNames() {
		return this.httpContext.getSession().getValueNames();
	}

	/**
	 * 
	 * @see javax.servlet.http.HttpSession#invalidate()
	 */
	public void invalidate() {
		this.httpContext.getSession().invalidate();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpSession#isNew()
	 */
	public boolean isNew() {
		return this.httpContext.getSession().isNew();
	}

	/**
	 * @param paramString
	 * @param paramObject
	 * @deprecated
	 * @see javax.servlet.http.HttpSession#putValue(java.lang.String, java.lang.Object)
	 */
	public void putValue(String paramString, Object paramObject) {
		this.httpContext.getSession().putValue(paramString, paramObject);
	}

	/**
	 * @param paramString
	 * @see javax.servlet.http.HttpSession#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String paramString) {
		this.httpContext.getSession().removeAttribute(paramString);
	}

	/**
	 * @param paramString
	 * @deprecated
	 * @see javax.servlet.http.HttpSession#removeValue(java.lang.String)
	 */
	public void removeValue(String paramString) {
		this.httpContext.getSession().removeValue(paramString);
	}

	/**
	 * @param paramString
	 * @param paramObject
	 * @see javax.servlet.http.HttpSession#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String paramString, Object paramObject) {
		this.httpContext.getSession().setAttribute(paramString, paramObject);
	}

	/**
	 * @param paramInt
	 * @see javax.servlet.http.HttpSession#setMaxInactiveInterval(int)
	 */
	public void setMaxInactiveInterval(int paramInt) {
		this.httpContext.getSession().setMaxInactiveInterval(paramInt);
	}
}
