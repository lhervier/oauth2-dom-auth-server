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
	 * Retourne la clé de cryptage à utiliser pour un plugin
	 * @param id l'id du plugin
	 */
	public static final String getSsoConfigFor(String id) {
		ParamsBean paramsBean = (ParamsBean) JSFUtils.getBean("paramsBean");
		if( paramsBean.getPluginsNames() == null )
			return null;
		if( paramsBean.getPluginsKeys() == null )
			throw new RuntimeException("Configuration invalide. On a des noms de plugin, mais pas de clé en face.");
		if( paramsBean.getPluginsNames().size() != paramsBean.getPluginsKeys().size() )
			throw new RuntimeException("Configuration invalide. On n'a pas le même nombre de valeurs dans le champs avec les noms des plugins, et celui avec leurs clés.");
		
		int pos = 0;
		boolean found = false;
		for(; pos<paramsBean.getPluginsNames().size(); pos++ ) {
			if( paramsBean.getPluginsNames().get(pos).equals(id) ) {
				found = true;
				break;
			}
		}
		if( !found )
			return null;
		
		return paramsBean.getPluginsKeys().get(pos);
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
