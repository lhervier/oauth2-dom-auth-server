package fr.asso.afer.oauth2.utils;

import javax.faces.context.FacesContext;

import lotus.domino.Database;
import lotus.domino.Session;

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
}
