package com.github.lhervier.domino.oauth.server.model.error.authorize;


/**
 * Le client n'est pas autorisé à demander un code autorisation
 * @author Lionel HERVIER
 */
public class UnauthorizedClientError extends AuthorizeError {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 6591668522230306467L;

	/**
	 * Constructeur
	 */
	public UnauthorizedClientError() {
		this.setError("unauthorized_client");
		this.setErrorDescription(
				"The client is not authorized to request an authorization " +
				"code using this method."
		);
		this.setErrorUri("http://lhervier.github.com/dom-auth-server/error/authorize/unauthorized_client");
	}
}
