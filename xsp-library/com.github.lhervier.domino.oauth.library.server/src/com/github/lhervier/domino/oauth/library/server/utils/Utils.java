package com.github.lhervier.domino.oauth.library.server.utils;

import java.security.PrivilegedActionException;
import java.util.List;

import com.github.lhervier.domino.oauth.common.utils.OsgiUtils;
import com.github.lhervier.domino.oauth.library.server.Activator;
import com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension;

/**
 * Méthodes utiles à l'appli
 * @author Lionel HERVIER
 */
public class Utils {

	/**
	 * Retourne la liste des extensions
	 * @return les extensions
	 */
	public static final List<IOAuthExtension> getExtensions() {
		try {
			return OsgiUtils.getExtensions(
					Activator.SCOPE_EXT_ID, 
					IOAuthExtension.class
			);
		} catch (PrivilegedActionException e) {
			throw new RuntimeException(e);
		}
	}
}
