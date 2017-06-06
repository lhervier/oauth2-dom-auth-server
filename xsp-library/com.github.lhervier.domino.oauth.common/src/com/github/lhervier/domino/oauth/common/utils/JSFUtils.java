package com.github.lhervier.domino.oauth.common.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import lotus.domino.Database;
import lotus.domino.Session;

import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.designer.context.XSPContext;

/**
 * Méthodes pratiques pour JSF
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
	 * Retourne la session courante
	 * @return la session
	 */
	public static final Session getSession() {
		return (Session) getBean("session");
	}
	
	/**
	 * Retourne la session ouverte avec les infos
	 * du signataire de la XPages courante
	 * @return la session
	 */
	public static final Session getSessionAsSigner() {
		return (Session) getBean("sessionAsSigner");
	}
	
	/**
	 * Retourne la database courante
	 * @return la database courante
	 */
	public static final Database getDatabase() {
		return (Database) getBean("database");
	}
	
	/**
	 * Retourne le contexte utilisateur
	 * @return le contexte utilisateur
	 */
	public static final XSPContext getContext() {
		return (XSPContext) getBean("context");
	}
	
	/**
	 * Retourne les paramètres de la requête
	 * @return les paramètres de la requête
	 */
	@SuppressWarnings("unchecked")
	public static final Map<String, String> getParam() {
		return (Map<String, String>) getBean("param");
	}
	
	/**
	 * Retourne le view root
	 * @return le view root
	 */
	public static final UIViewRootEx getView() {
		return (UIViewRootEx) getBean("view");
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
	 * Retourne la requête http
	 * @return la requête http
	 */
	public static final HttpServletRequest getServletRequest() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		return (HttpServletRequest) ctx.getExternalContext().getRequest();
	}
	
	/**
	 * Retourne la réponse http
	 * @return la réponse http
	 */
	public static final HttpServletResponse getServletResponse() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		return (HttpServletResponse) ctx.getExternalContext().getResponse();
	}
	
	/**
	 * Envoi du Json en réponse dans la stream HTTP
	 * @param obj l'objet à envoyer
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
	 * @param url l'url où rediriger
	 */
	public static final void sendRedirect(String location) {
		HttpServletResponse response = JSFUtils.getServletResponse();
		response.setStatus(302);
		response.setHeader("Location", location);
		FacesContext.getCurrentInstance().responseComplete();
	}
	
	/**
	 * Renvoi une erreur 404
	 * @throws IOException 
	 */
	public static final void send404() throws IOException {
		OutputStream out = null;
		Writer writer = null;
		try {
			HttpServletResponse resp = JSFUtils.getServletResponse();
			resp.setStatus(404);
			out = resp.getOutputStream();
			writer = new OutputStreamWriter(out, "UTF-8");
			writer.write(
					"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n" +
					"<html>\n" +
					"<head>\n" +
					"<title>Error</title></head>\n" +
					"<body text=\"#000000\">\n" +
					"<h1>Error 404</h1>HTTP Web Server: Item Not Found Exception</body>\n" +
					"</html>"
			);
		} finally {
			IOUtils.closeQuietly(writer);
			IOUtils.closeQuietly(out);
		}
		System.err.println(
				"ATTENTION: L'utilisateur '" + JSFUtils.getContext().getUser().getFullName() + "' " +
				"a tenté d'accéder à la page '" +JSFUtils.getView().getPageName() + "'"
		);
		FacesContext.getCurrentInstance().responseComplete();
	}
}
