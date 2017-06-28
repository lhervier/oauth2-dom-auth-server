package com.github.lhervier.domino.oauth.library.server.ext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class WrappedHttpServletRequest implements HttpServletRequest {

	private HttpContext httpContext;
	
	public WrappedHttpServletRequest(HttpContext httpCtx) {
		this.httpContext = httpCtx;
	}
	
	/**
	 * @param paramString
	 * @return
	 * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String paramString) {
		return this.httpContext.getRequest().getAttribute(paramString);
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getAttributeNames()
	 */
	@SuppressWarnings("unchecked")
	public Enumeration getAttributeNames() {
		return this.httpContext.getRequest().getAttributeNames();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getAuthType()
	 */
	public String getAuthType() {
		return this.httpContext.getRequest().getAuthType();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		return this.httpContext.getRequest().getCharacterEncoding();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getContentLength()
	 */
	public int getContentLength() {
		return this.httpContext.getRequest().getContentLength();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getContentType()
	 */
	public String getContentType() {
		return this.httpContext.getRequest().getContentType();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getContextPath()
	 */
	public String getContextPath() {
		return this.httpContext.getRequest().getContextPath();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getCookies()
	 */
	public Cookie[] getCookies() {
		return this.httpContext.getRequest().getCookies();
	}

	/**
	 * @param paramString
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
	 */
	public long getDateHeader(String paramString) {
		return this.httpContext.getRequest().getDateHeader(paramString);
	}

	/**
	 * @param paramString
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
	 */
	public String getHeader(String paramString) {
		return this.httpContext.getRequest().getHeader(paramString);
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
	 */
	@SuppressWarnings("unchecked")
	public Enumeration getHeaderNames() {
		return this.httpContext.getRequest().getHeaderNames();
	}

	/**
	 * @param paramString
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Enumeration getHeaders(String paramString) {
		return this.httpContext.getRequest().getHeaders(paramString);
	}

	/**
	 * @return
	 * @throws IOException
	 * @see javax.servlet.ServletRequest#getInputStream()
	 */
	public ServletInputStream getInputStream() throws IOException {
		return this.httpContext.getRequest().getInputStream();
	}

	/**
	 * @param paramString
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
	 */
	public int getIntHeader(String paramString) {
		return this.httpContext.getRequest().getIntHeader(paramString);
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getLocalAddr()
	 */
	public String getLocalAddr() {
		return this.httpContext.getRequest().getLocalAddr();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getLocalName()
	 */
	public String getLocalName() {
		return this.httpContext.getRequest().getLocalName();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getLocalPort()
	 */
	public int getLocalPort() {
		return this.httpContext.getRequest().getLocalPort();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getLocale()
	 */
	public Locale getLocale() {
		return this.httpContext.getRequest().getLocale();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getLocales()
	 */
	@SuppressWarnings("unchecked")
	public Enumeration getLocales() {
		return this.httpContext.getRequest().getLocales();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getMethod()
	 */
	public String getMethod() {
		return this.httpContext.getRequest().getMethod();
	}

	/**
	 * @param paramString
	 * @return
	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
	 */
	public String getParameter(String paramString) {
		return this.httpContext.getRequest().getParameter(paramString);
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	@SuppressWarnings("unchecked")
	public Map getParameterMap() {
		return this.httpContext.getRequest().getParameterMap();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getParameterNames()
	 */
	@SuppressWarnings("unchecked")
	public Enumeration getParameterNames() {
		return this.httpContext.getRequest().getParameterNames();
	}

	/**
	 * @param paramString
	 * @return
	 * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String paramString) {
		return this.httpContext.getRequest().getParameterValues(paramString);
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getPathInfo()
	 */
	public String getPathInfo() {
		return this.httpContext.getRequest().getPathInfo();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
	 */
	public String getPathTranslated() {
		return this.httpContext.getRequest().getPathTranslated();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getProtocol()
	 */
	public String getProtocol() {
		return this.httpContext.getRequest().getProtocol();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getQueryString()
	 */
	public String getQueryString() {
		return this.httpContext.getRequest().getQueryString();
	}

	/**
	 * @return
	 * @throws IOException
	 * @see javax.servlet.ServletRequest#getReader()
	 */
	public BufferedReader getReader() throws IOException {
		return this.httpContext.getRequest().getReader();
	}

	/**
	 * @param paramString
	 * @return
	 * @deprecated
	 * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
	 */
	public String getRealPath(String paramString) {
		return this.httpContext.getRequest().getRealPath(paramString);
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getRemoteAddr()
	 */
	public String getRemoteAddr() {
		return this.httpContext.getRequest().getRemoteAddr();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getRemoteHost()
	 */
	public String getRemoteHost() {
		return this.httpContext.getRequest().getRemoteHost();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getRemotePort()
	 */
	public int getRemotePort() {
		return this.httpContext.getRequest().getRemotePort();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
	 */
	public String getRemoteUser() {
		return this.httpContext.getRequest().getRemoteUser();
	}

	/**
	 * @param paramString
	 * @return
	 * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
	 */
	public RequestDispatcher getRequestDispatcher(String paramString) {
		return this.httpContext.getRequest().getRequestDispatcher(paramString);
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI()
	 */
	public String getRequestURI() {
		return this.httpContext.getRequest().getRequestURI();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getRequestURL()
	 */
	public StringBuffer getRequestURL() {
		return this.httpContext.getRequest().getRequestURL();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
	 */
	public String getRequestedSessionId() {
		return this.httpContext.getRequest().getRequestedSessionId();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getScheme()
	 */
	public String getScheme() {
		return this.httpContext.getRequest().getScheme();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getServerName()
	 */
	public String getServerName() {
		return this.httpContext.getRequest().getServerName();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getServerPort()
	 */
	public int getServerPort() {
		return this.httpContext.getRequest().getServerPort();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getServletPath()
	 */
	public String getServletPath() {
		return this.httpContext.getRequest().getServletPath();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getSession()
	 */
	public HttpSession getSession() {
		return this.httpContext.getRequest().getSession();
	}

	/**
	 * @param paramBoolean
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	public HttpSession getSession(boolean paramBoolean) {
		return this.httpContext.getRequest().getSession(paramBoolean);
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	public Principal getUserPrincipal() {
		return this.httpContext.getRequest().getUserPrincipal();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
	 */
	public boolean isRequestedSessionIdFromCookie() {
		return this.httpContext.getRequest().isRequestedSessionIdFromCookie();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
	 */
	public boolean isRequestedSessionIdFromURL() {
		return this.httpContext.getRequest().isRequestedSessionIdFromURL();
	}

	/**
	 * @return
	 * @deprecated
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
	 */
	public boolean isRequestedSessionIdFromUrl() {
		return this.httpContext.getRequest().isRequestedSessionIdFromUrl();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
	 */
	public boolean isRequestedSessionIdValid() {
		return this.httpContext.getRequest().isRequestedSessionIdValid();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#isSecure()
	 */
	public boolean isSecure() {
		return this.httpContext.getRequest().isSecure();
	}

	/**
	 * @param paramString
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
	 */
	public boolean isUserInRole(String paramString) {
		return this.httpContext.getRequest().isUserInRole(paramString);
	}

	/**
	 * @param paramString
	 * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String paramString) {
		this.httpContext.getRequest().removeAttribute(paramString);
	}

	/**
	 * @param paramString
	 * @param paramObject
	 * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String paramString, Object paramObject) {
		this.httpContext.getRequest().setAttribute(paramString, paramObject);
	}

	/**
	 * @param paramString
	 * @throws UnsupportedEncodingException
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String paramString)
			throws UnsupportedEncodingException {
		this.httpContext.getRequest().setCharacterEncoding(paramString);
	}
}
