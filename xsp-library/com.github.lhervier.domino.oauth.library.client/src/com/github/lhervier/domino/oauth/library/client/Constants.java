package com.github.lhervier.domino.oauth.library.client;

public class Constants {

	/**
	 * Nom de la variable du notes.ini pour d�sativer la v�rification SSL
	 * lors de l'appel de serveur � serveur
	 */
	public static final String NOTES_INI_DISABLE_CHECK_CERTIFICATE = "DOMFRONT_DISABLE_CHECK_CERTIFICATE";
	
	/**
	 * Session attribute for access token
	 */
	public static final String SESSION_ACCESS_TOKEN = "ACCESS_TOKEN";
	
	/**
	 * Session attribute for refresh token
	 */
	public static final String SESSION_REFRESH_TOKEN = "REFRESH_TOKEN";
	
	/**
	 * Session attribute for id token
	 */
	public static final String SESSION_ID_TOKEN = "ID_TOKEN";
}
