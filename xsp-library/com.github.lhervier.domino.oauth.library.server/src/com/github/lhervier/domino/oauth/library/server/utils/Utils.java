package com.github.lhervier.domino.oauth.library.server.utils;

import java.io.IOException;
import java.security.PrivilegedActionException;
import java.util.List;

import com.github.lhervier.domino.oauth.common.utils.JSFUtils;
import com.github.lhervier.domino.oauth.common.utils.OsgiUtils;
import com.github.lhervier.domino.oauth.library.server.Activator;
import com.github.lhervier.domino.oauth.library.server.bean.ParamsBean;
import com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension;

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
	
	/**
	 * Retourne l'issuer en s'assurant qu'il n'y a pas de / à la fin
	 * @l'issuer
	 */
	public static final String getIssuer() {
		ParamsBean paramsBean = (ParamsBean) JSFUtils.getBean("paramsBean");
		String iss = paramsBean.getIssuer();
		if( iss.endsWith("/") )
			return iss.substring(0, iss.length() - 1);
		return iss;
	}
	
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
