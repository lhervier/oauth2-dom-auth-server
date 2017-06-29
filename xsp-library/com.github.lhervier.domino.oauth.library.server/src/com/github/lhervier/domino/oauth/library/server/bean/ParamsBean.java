package com.github.lhervier.domino.oauth.library.server.bean;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lotus.domino.NotesException;

import com.github.lhervier.domino.oauth.common.bean.BaseParamsBean;
import com.github.lhervier.domino.oauth.common.utils.GsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Bean pour accéder aux paramètres de l'application
 * @author Lionel HERVIER
 */
public class ParamsBean extends BaseParamsBean {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 25035453476284192L;

	/**
	 * La conf par plugin
	 */
	private Map<String, JsonObject> confByPlugin = null;
	
	/**
	 * Constructeur
	 * @throws NotesException 
	 */
	public ParamsBean() throws NotesException {
		this.setViewName("Params");
		this.setPrefix("");
	}
	
	/**
	 * Retourne la conf d'un plugin
	 * @param pluginId l'id du plugin
	 * @return sa conf
	 */
	public synchronized JsonObject getPluginConf(String pluginId) {
		if( this.confByPlugin == null ) {
			this.confByPlugin = new HashMap<String, JsonObject>();
			JsonObject conf = GsonUtils.fromJson(this.pluginsConfs);
			for( Entry<String, JsonElement> entry : conf.entrySet() ) {
				this.confByPlugin.put(entry.getKey(), (JsonObject) entry.getValue());
			}
		}
		return this.confByPlugin.get(pluginId);
	}
	
	
	// ======================================================================
	//				Propriété remplies par la classe parente
	// ======================================================================
	
	/**
	 * Le carnet d'adresse où créer les applications
	 */
	private String nab;
	
	/**
	 * Pour compléter les noms des applications
	 */
	private String applicationRoot;
	
	/**
	 * La durée de vie du refresh token
	 */
	private long refreshTokenLifetime;
	
	/**
	 * Le nom de la config SSO qui contient la clé pour le refresh token
	 */
	private String refreshTokenConfig;
	
	/**
	 * La durée de vie des codes autorisation
	 */
	private long authCodeLifetime;
	
	/**
	 * Les noms des clés à utiliser pour chaque plugin
	 */
	private String pluginsConfs;
	
	// =============================================================

	/**
	 * @return the nab
	 */
	public String getNab() {
		return nab;
	}

	/**
	 * @param nab the nab to set
	 */
	public void setNab(String nab) {
		this.nab = nab;
	}

	/**
	 * @return the refreshTokenLifetime
	 */
	public long getRefreshTokenLifetime() {
		return refreshTokenLifetime;
	}

	/**
	 * @param refreshTokenLifetime the refreshTokenLifetime to set
	 */
	public void setRefreshTokenLifetime(long refreshTokenLifetime) {
		this.refreshTokenLifetime = refreshTokenLifetime;
	}

	/**
	 * @return the refreshTokenConfig
	 */
	public String getRefreshTokenConfig() {
		return refreshTokenConfig;
	}

	/**
	 * @param refreshTokenConfig the refreshTokenConfig to set
	 */
	public void setRefreshTokenConfig(String refreshTokenConfig) {
		this.refreshTokenConfig = refreshTokenConfig;
	}

	/**
	 * @return the authCodeLifetime
	 */
	public long getAuthCodeLifetime() {
		return authCodeLifetime;
	}

	/**
	 * @param authCodeLifetime the authCodeLifetime to set
	 */
	public void setAuthCodeLifetime(long authCodeLifetime) {
		this.authCodeLifetime = authCodeLifetime;
	}

	/**
	 * @return the applicationRoot
	 */
	public String getApplicationRoot() {
		return applicationRoot;
	}

	/**
	 * @param applicationRoot the applicationRoot to set
	 */
	public void setApplicationRoot(String applicationRoot) {
		this.applicationRoot = applicationRoot;
	}

	/**
	 * @return the pluginsConfs
	 */
	public String getPluginsConfs() {
		return pluginsConfs;
	}

	/**
	 * @param pluginsConfs the pluginsConfs to set
	 */
	public void setPluginsConfs(String pluginsKeys) {
		this.pluginsConfs = pluginsKeys;
	}
}
