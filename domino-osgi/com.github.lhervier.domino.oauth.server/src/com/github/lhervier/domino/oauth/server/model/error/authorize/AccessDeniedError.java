package com.github.lhervier.domino.oauth.server.model.error.authorize;


/**
 * Erreur d'autorisation
 * @author Lionel HERVIER
 */
public class AccessDeniedError extends AuthorizeError {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 319254282380254073L;

	/**
	 * Constructeur
	 */
	public AccessDeniedError() {
		this.setError("access_denied");
		this.setErrorDescription("The resource owner or authorization server denied the request.");
		this.setErrorUri("http://lhervier.github.com/dom-auth-server/error/authorize/access_denied");
	}
}
