package com.github.lhervier.domino.oauth.library.server.utils;

import javax.servlet.http.HttpServletRequest;

import lotus.domino.Database;

import com.github.lhervier.domino.oauth.common.utils.DatabaseWrapper;
import com.github.lhervier.domino.spring.servlet.NotesContext;

/**
 * Méthodes utiles à l'appli
 * @author Lionel HERVIER
 */
public class Utils {

	/**
	 * Return the oauth2 database
	 */
	public static final Database getOauth2Database(
			HttpServletRequest request,
			NotesContext notesContext,
			String oauth2db) {
		String key = Utils.class.getName() + ".oauth2db";
		if( request.getAttribute(key) != null )
			return (Database) request.getAttribute(key);
		
		DatabaseWrapper nab = new DatabaseWrapper(notesContext, oauth2db, true);
		request.setAttribute(key, nab);
		
		return nab;
	}
}
