package com.github.lhervier.domino.oauth.common.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * M�thodes pratiques pour JSF
 * @author Lionel HERVIER
 */
public class JSFUtils {

	/**
	 * Retourne une managed bean
	 * @param name le nom de la bean
	 * @return la bean
	 */
	public static final Object getBean(String name) {
		FacesContext ctx = FacesContext.getCurrentInstance();
		return ctx.getApplication().getVariableResolver().resolveVariable(ctx, name);
	}
	
	/**
	 * Retourne les param�tres de la requ�te
	 * @return les param�tres de la requ�te
	 */
	@SuppressWarnings("unchecked")
	public static final Map<String, String> getParam() {
		return (Map<String, String>) getBean("param");
	}
	
	/**
	 * Retourne le request scope
	 * @return le request scope
	 */
	@SuppressWarnings("unchecked")
	public static final Map<String, Object> getRequestScope() {
		return (Map<String, Object>) getBean("requestScope");
	}
	
	/**
	 * Retourne le session scope
	 * @return le session scope
	 */
	@SuppressWarnings("unchecked")
	public static final Map<String, Object> getSessionScope() {
		return (Map<String, Object>) getBean("sessionScope");
	}
	
	/**
	 * Retourne la requ�te http
	 * @return la requ�te http
	 */
	public static final HttpServletRequest getServletRequest() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		return (HttpServletRequest) ctx.getExternalContext().getRequest();
	}
	
	/**
	 * Retourne la r�ponse http
	 * @return la r�ponse http
	 */
	public static final HttpServletResponse getServletResponse() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		return (HttpServletResponse) ctx.getExternalContext().getResponse();
	}
	
	/**
	 * Envoi du Json en r�ponse dans la stream HTTP
	 * @param obj l'objet � envoyer
	 * @throws IOException 
	 */
	public static final void sendJson(Object o) throws IOException {
		HttpServletResponse response = JSFUtils.getServletResponse();
		OutputStream out = null;
		OutputStreamWriter wrt = null;
		try {
			response.setContentType("application/json;charset=UTF-8");
			response.setHeader("Cache-Control", "no-store");
			response.setHeader("Pragma", "no-cache");
			if( o != null ) {
				out = response.getOutputStream();
				wrt = new OutputStreamWriter(out, "UTF-8");
				wrt.write(GsonUtils.toJson(o));
			}
		} finally {
			IOUtils.closeQuietly(wrt);
			IOUtils.closeQuietly(out);
		}
		FacesContext.getCurrentInstance().responseComplete();
	}
	
	/**
	 * Envoi un 403
	 */
	public static final void send403() {
		HttpServletResponse resp = JSFUtils.getServletResponse();
		resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
		FacesContext.getCurrentInstance().responseComplete();
	}
	
	/**
	 * Redirige
	 * @param url l'url o� rediriger
	 */
	public static final void sendRedirect(String location) {
		HttpServletResponse response = JSFUtils.getServletResponse();
		response.setStatus(302);
		response.setHeader("Location", location);
		FacesContext.getCurrentInstance().responseComplete();
	}
}
