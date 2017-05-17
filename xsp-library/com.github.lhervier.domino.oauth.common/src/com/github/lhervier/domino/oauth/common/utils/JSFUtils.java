package com.github.lhervier.domino.oauth.common.utils;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
}
