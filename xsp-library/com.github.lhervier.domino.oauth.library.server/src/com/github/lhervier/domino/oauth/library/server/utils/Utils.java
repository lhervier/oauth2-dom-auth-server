package com.github.lhervier.domino.oauth.library.server.utils;

import java.io.IOException;

import com.github.lhervier.domino.oauth.common.utils.JSFUtils;

/**
 * Méthodes utiles à l'appli
 * @author Lionel HERVIER
 */
public class Utils {

	/**
	 * Pour vérifier si un utilisateur a un rôle. Renvoi un 404 si
	 * ce n'est pas le cas
	 * @param role le role
	 * @throws IOException 
	 */
	public static final void checkRole(String role) throws IOException {
		if( !JSFUtils.getContext().getUser().getRoles().contains(role) )
			JSFUtils.send404();
	}
}
