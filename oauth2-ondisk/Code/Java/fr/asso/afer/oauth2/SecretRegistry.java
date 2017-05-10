package fr.asso.afer.oauth2;

import fr.asso.afer.oauth2.utils.DominoUtils;
import fr.asso.afer.oauth2.utils.JSFUtils;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

/**
 * Registre pour mémoriser les secrets
 * @author Lionel HERVIER
 */
public class SecretRegistry {

	/**
	 * Le rôle pour pouvoir utiliser cette classe
	 */
	public static final String ROLE = "[SecretExtractor]";
	
	/**
	 * Le nom de la vue qui contient les configs SSO
	 */
	public static final String WEBSSOCONFIG_VIEW = "($WebSSOConfigs)";
	
	/**
	 * Le nom du champ dans lequel récupérer le secret
	 */
	public static final String SECRET_FIELD_NAME = "LTPA_DominoSecret";
	
	/**
	 * Retourne la session
	 * @return la session
	 */
	private Session getSession() {
		return JSFUtils.getSessionAsSigner();
	}
	
	/**
	 * Retourne le document config SSO
	 * @param config le nom de la config à extraire
	 * @return la config SSO
	 * @throws NotesException en cas de pb
	 */
	private Document getSsoConfig(String config) throws NotesException {
		Session session = JSFUtils.getSession();
		
		Database db = JSFUtils.getDatabase();
		if( !db.queryAccessRoles(session.getEffectiveUserName()).contains(ROLE) )
			throw new RuntimeException("Vous n'avez pas les droits pour extraire les secrets");
		
		Database names = DominoUtils.openDatabase(this.getSession(), JSFUtils.getParamsBean().getNab());
		if( names == null )
			throw new RuntimeException("Je n'arrive pas à accéder à la base " + JSFUtils.getParamsBean().getNab());
		
		View v = names.getView(WEBSSOCONFIG_VIEW);
		if( v == null )
			throw new RuntimeException("La vue " + WEBSSOCONFIG_VIEW + " n'existe pas dans la base '" + JSFUtils.getParamsBean().getNab() + "'. Impossible de continuer.");
		Document ssoConfig = v.getDocumentByKey("AFER:AccessToken");
		if( ssoConfig == null )
			throw new RuntimeException("Je ne trouve pas la confg SSO '" + config + "'");
		
		return ssoConfig;
	}
	
	/**
	 * Retourne le secret utilisé pour signer l'access token
	 */
	public String getAccessTokenSecret() throws NotesException {
		return this.getSsoConfig(Constants.CONFIG_NAME_ACCESS_TOKEN).getItemValueString(SECRET_FIELD_NAME);
	}
	
	/**
	 * Retourne le secret utilisé pour crypter le refresh token
	 */
	public String getRefreshTokenSecret() throws NotesException {
		return this.getSsoConfig(Constants.CONFIG_NAME_REFRESH_TOKEN).getItemValueString(SECRET_FIELD_NAME);
	}
}
