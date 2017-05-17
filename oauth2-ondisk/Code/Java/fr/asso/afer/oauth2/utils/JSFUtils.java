package fr.asso.afer.oauth2.utils;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lotus.domino.Database;
import lotus.domino.Session;

import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.designer.context.XSPContext;

import fr.asso.afer.oauth2.bean.AppBean;
import fr.asso.afer.oauth2.bean.ParamsBean;
import fr.asso.afer.oauth2.bean.SecretBean;

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
	
	// =====================================================================
	
	/**
	 * Retourne le requestScope
	 * @return le requestScope
	 */
	@SuppressWarnings("unchecked")
	public static final Map<String, Object> getRequestScope() {
		return (Map<String, Object>) getBean("requestScope");
	}
	
	/**
	 * Retourne la bean de paramétrage
	 * @return la bean de paramétrage
	 */
	public static final ParamsBean getParamsBean() {
		return (ParamsBean) getBean("paramsBean");
	}
	
	/**
	 * Retourne la bean pour gérer les apps
	 * @return la bean pour gérer les apps
	 */
	public static final AppBean getAppBean() {
		return (AppBean) getBean("appBean");
	}
	
	/**
	 * Retourne la bean pour accéder aux secrets
	 * @return la secretBean
	 */
	public static final SecretBean getSecretBean() {
		return (SecretBean) getBean("secretBean");
	}
	
}
