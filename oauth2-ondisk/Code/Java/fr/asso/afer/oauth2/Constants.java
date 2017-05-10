package fr.asso.afer.oauth2;

/**
 * Des constantes
 * @author Lionel HERVIER
 */
public class Constants {

	/**
	 * Le names dans lequel rechercher les configs SSO
	 */
	public static final String NAMES_PATH = "names.nsf";
	
	/**
	 * Le nom du groupe qui contient l'ensemble des applications
	 */
	public static final String APPLICATIONS_GROUP = "OAUTH2Applications";
	
	/**
	 * Le nom de la config SSO depuis laquelle on va extraire le secret pour signer le access token
	 */
	public static final String ACCESS_TOKEN_CONFIG_NAME = "AFER:AccessToken";
	
	/**
	 * Le nom de la config SSO depuis laquelle on va extraire le secret pour crypter le refresh token
	 */
	public static final String REFRESH_TOKEN_CONFIG_NAME = "AFER:RefreshToken";
	
	/**
	 * Le nom du champ qui contient le client_id des applications (dans un document Person)
	 */
	public static final String CLIENT_ID_FIELD_NAME = "OAUTH2_client_id";
	
	/**
	 * Le nom du champ qui contient l'URL de redirection par défaut d'une application (dans un document Person)
	 */
	public static final String REDIRECT_URI_FIELD_NAME = "OAUTH2_redirect_uri";
	
	/**
	 * Le nom du champ qui contient les autres URL de redirection d'une application (dans un document Person)
	 */
	public static final String REDIRECT_URIS_FIELD_NAME = "OAUTH2_redirect_uris";
	
	/**
	 * Le suffixe des noms des applications
	 */
	public static final String APP_NAME_SUFFIX = "/APPLICATION/WEB";
	
}
