package com.github.lhervier.domino.oauth.common.spring.wrap;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.github.lhervier.domino.oauth.common.spring.ctx.HttpContext;

public class WrappedHttpServletResponse implements HttpServletResponse {

	private HttpContext httpContext;
	
	public WrappedHttpServletResponse(HttpContext httpCtx) {
		this.httpContext = httpCtx;
	}
	
	/**
	 * @param paramCookie
	 * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
	 */
	public void addCookie(Cookie paramCookie) {
		this.httpContext.getResponse().addCookie(paramCookie);
	}

	/**
	 * @param paramString
	 * @param paramLong
	 * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
	 */
	public void addDateHeader(String paramString, long paramLong) {
		this.httpContext.getResponse().addDateHeader(paramString, paramLong);
	}

	/**
	 * @param paramString1
	 * @param paramString2
	 * @see javax.servlet.http.HttpServletthis.httpContext.getResponse()#addHeader(java.lang.String, java.lang.String)
	 */
	public void addHeader(String paramString1, String paramString2) {
		this.httpContext.getResponse().addHeader(paramString1, paramString2);
	}

	/**
	 * @param paramString
	 * @param paramInt
	 * @see javax.servlet.http.HttpServletthis.httpContext.getResponse()#addIntHeader(java.lang.String, int)
	 */
	public void addIntHeader(String paramString, int paramInt) {
		this.httpContext.getResponse().addIntHeader(paramString, paramInt);
	}

	/**
	 * @param paramString
	 * @return
	 * @see javax.servlet.http.HttpServletthis.httpContext.getResponse()#containsHeader(java.lang.String)
	 */
	public boolean containsHeader(String paramString) {
		return this.httpContext.getResponse().containsHeader(paramString);
	}

	/**
	 * @param paramString
	 * @return
	 * @see javax.servlet.http.HttpServletthis.httpContext.getResponse()#encodeRedirectURL(java.lang.String)
	 */
	public String encodeRedirectURL(String paramString) {
		return this.httpContext.getResponse().encodeRedirectURL(paramString);
	}

	/**
	 * @param paramString
	 * @return
	 * @deprecated
	 * @see javax.servlet.http.HttpServletthis.httpContext.getResponse()#encodeRedirectUrl(java.lang.String)
	 */
	public String encodeRedirectUrl(String paramString) {
		return this.httpContext.getResponse().encodeRedirectUrl(paramString);
	}

	/**
	 * @param paramString
	 * @return
	 * @see javax.servlet.http.HttpServletthis.httpContext.getResponse()#encodeURL(java.lang.String)
	 */
	public String encodeURL(String paramString) {
		return this.httpContext.getResponse().encodeURL(paramString);
	}

	/**
	 * @param paramString
	 * @return
	 * @deprecated
	 * @see javax.servlet.http.HttpServletthis.httpContext.getResponse()#encodeUrl(java.lang.String)
	 */
	public String encodeUrl(String paramString) {
		return this.httpContext.getResponse().encodeUrl(paramString);
	}

	/**
	 * @throws IOException
	 * @see javax.servlet.Servletthis.httpContext.getResponse()#flushBuffer()
	 */
	public void flushBuffer() throws IOException {
		this.httpContext.getResponse().flushBuffer();
	}

	/**
	 * @return
	 * @see javax.servlet.Servletthis.httpContext.getResponse()#getBufferSize()
	 */
	public int getBufferSize() {
		return this.httpContext.getResponse().getBufferSize();
	}

	/**
	 * @return
	 * @see javax.servlet.Servletthis.httpContext.getResponse()#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		return this.httpContext.getResponse().getCharacterEncoding();
	}

	/**
	 * @return
	 * @see javax.servlet.Servletthis.httpContext.getResponse()#getContentType()
	 */
	public String getContentType() {
		return this.httpContext.getResponse().getContentType();
	}

	/**
	 * @return
	 * @see javax.servlet.Servletthis.httpContext.getResponse()#getLocale()
	 */
	public Locale getLocale() {
		return this.httpContext.getResponse().getLocale();
	}

	/**
	 * @return
	 * @throws IOException
	 * @see javax.servlet.Servletthis.httpContext.getResponse()#getOutputStream()
	 */
	public ServletOutputStream getOutputStream() throws IOException {
		return this.httpContext.getResponse().getOutputStream();
	}

	/**
	 * @return
	 * @throws IOException
	 * @see javax.servlet.Servletthis.httpContext.getResponse()#getWriter()
	 */
	public PrintWriter getWriter() throws IOException {
		return this.httpContext.getResponse().getWriter();
	}

	/**
	 * @return
	 * @see javax.servlet.Servletthis.httpContext.getResponse()#isCommitted()
	 */
	public boolean isCommitted() {
		return this.httpContext.getResponse().isCommitted();
	}

	/**
	 * 
	 * @see javax.servlet.Servletthis.httpContext.getResponse()#reset()
	 */
	public void reset() {
		this.httpContext.getResponse().reset();
	}

	/**
	 * 
	 * @see javax.servlet.Servletthis.httpContext.getResponse()#resetBuffer()
	 */
	public void resetBuffer() {
		this.httpContext.getResponse().resetBuffer();
	}

	/**
	 * @param paramInt
	 * @throws IOException
	 * @see javax.servlet.http.HttpServletthis.httpContext.getResponse()#sendError(int)
	 */
	public void sendError(int paramInt) throws IOException {
		this.httpContext.getResponse().sendError(paramInt);
	}

	/**
	 * @param paramInt
	 * @param paramString
	 * @throws IOException
	 * @see javax.servlet.http.HttpServletthis.httpContext.getResponse()#sendError(int, java.lang.String)
	 */
	public void sendError(int paramInt, String paramString) throws IOException {
		this.httpContext.getResponse().sendError(paramInt, paramString);
	}

	/**
	 * @param paramString
	 * @throws IOException
	 * @see javax.servlet.http.HttpServletthis.httpContext.getResponse()#sendRedirect(java.lang.String)
	 */
	public void sendRedirect(String paramString) throws IOException {
		this.httpContext.getResponse().sendRedirect(paramString);
	}

	/**
	 * @param paramInt
	 * @see javax.servlet.Servletthis.httpContext.getResponse()#setBufferSize(int)
	 */
	public void setBufferSize(int paramInt) {
		this.httpContext.getResponse().setBufferSize(paramInt);
	}

	/**
	 * @param paramString
	 * @see javax.servlet.Servletthis.httpContext.getResponse()#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String paramString) {
		this.httpContext.getResponse().setCharacterEncoding(paramString);
	}

	/**
	 * @param paramInt
	 * @see javax.servlet.Servletthis.httpContext.getResponse()#setContentLength(int)
	 */
	public void setContentLength(int paramInt) {
		this.httpContext.getResponse().setContentLength(paramInt);
	}

	/**
	 * @param paramString
	 * @see javax.servlet.Servletthis.httpContext.getResponse()#setContentType(java.lang.String)
	 */
	public void setContentType(String paramString) {
		this.httpContext.getResponse().setContentType(paramString);
	}

	/**
	 * @param paramString
	 * @param paramLong
	 * @see javax.servlet.http.HttpServletthis.httpContext.getResponse()#setDateHeader(java.lang.String, long)
	 */
	public void setDateHeader(String paramString, long paramLong) {
		this.httpContext.getResponse().setDateHeader(paramString, paramLong);
	}

	/**
	 * @param paramString1
	 * @param paramString2
	 * @see javax.servlet.http.HttpServletthis.httpContext.getResponse()#setHeader(java.lang.String, java.lang.String)
	 */
	public void setHeader(String paramString1, String paramString2) {
		this.httpContext.getResponse().setHeader(paramString1, paramString2);
	}

	/**
	 * @param paramString
	 * @param paramInt
	 * @see javax.servlet.http.HttpServletthis.httpContext.getResponse()#setIntHeader(java.lang.String, int)
	 */
	public void setIntHeader(String paramString, int paramInt) {
		this.httpContext.getResponse().setIntHeader(paramString, paramInt);
	}

	/**
	 * @param paramLocale
	 * @see javax.servlet.Servletthis.httpContext.getResponse()#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale paramLocale) {
		this.httpContext.getResponse().setLocale(paramLocale);
	}

	/**
	 * @param paramInt
	 * @see javax.servlet.http.HttpServletthis.httpContext.getResponse()#setStatus(int)
	 */
	public void setStatus(int paramInt) {
		this.httpContext.getResponse().setStatus(paramInt);
	}

	/**
	 * @param paramInt
	 * @param paramString
	 * @deprecated
	 * @see javax.servlet.http.HttpServletthis.httpContext.getResponse()#setStatus(int, java.lang.String)
	 */
	public void setStatus(int paramInt, String paramString) {
		this.httpContext.getResponse().setStatus(paramInt, paramString);
	}
}
