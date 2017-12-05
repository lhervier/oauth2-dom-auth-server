package com.github.lhervier.domino.oauth.server.ex;

import com.github.lhervier.domino.oauth.server.model.error.authorize.AuthorizeError;

/**
 * Erreur pendant l'exécution du code grand 'authorize'
 * @author Lionel HERVIER
 */
public abstract class BaseAuthException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -2886333739068088636L;

	/**
	 * L'erreur associée
	 */
	private AuthorizeError error;
	
	/**
	 * The redirect uri
	 */
	private String redirectUri;
	
	/**
	 * Constructeur
	 * @param redirectUri the redirect uri
	 * @param error l'erreur
	 */
	public BaseAuthException(String message, String redirectUri, AuthorizeError error) {
		super(message);
		this.redirectUri = redirectUri;
		this.error = error;
		this.error.setErrorDescription(message);
	}
	
	/**
	 * @return the error
	 */
	public AuthorizeError getError() {
		return error;
	}

	/**
	 * @return the redirectUri
	 */
	public String getRedirectUri() {
		return redirectUri;
	}

	
}
