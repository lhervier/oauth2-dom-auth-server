package fr.asso.afer.oauth2;

/**
 * Des constantes
 * @author Lionel HERVIER
 */
public class Constants {

	/**
	 * Le nom de la config SSO depuis laquelle on va extraire le secret pour signer le access token
	 */
	public static final String CONFIG_NAME_ACCESS_TOKEN = "AFER:AccessToken";
	
	/**
	 * Le nom de la config SSO depuis laquelle on va extraire le secret pour crypter le refresh token
	 */
	public static final String CONFIG_NAME_REFRESH_TOKEN = "AFER:RefreshToken";
	
	/**
	 * Le nom du champ qui contient le client_id des applications (dans un document Person)
	 */
	public static final String FIELD_CLIENT_ID = "OAUTH2_client_id";
	
	/**
	 * Le nom du champ qui contient l'URL de redirection par défaut d'une application (dans un document Person)
	 */
	public static final String FIELF_REDIRECT_URI = "OAUTH2_redirect_uri";
	
	/**
	 * Le nom du champ qui contient les autres URL de redirection d'une application (dans un document Person)
	 */
	public static final String FIELD_REDIRECT_URIS = "OAUTH2_redirect_uris";
	
	/**
	 * Le suffixe des noms des applications
	 */
	public static final String SUFFIX_APP = "/APPLICATION/WEB";
	
	/**
	 * Le nom du rôle pour pouvoir gérer les applications
	 */
	public static final String ROLE_APPSMANAGER = "[AppsManager]";
	
}
