package com.github.lhervier.domino.oauth.server.ex.authorize;

import com.github.lhervier.domino.oauth.server.ex.BaseAuthException;
import com.github.lhervier.domino.oauth.server.model.error.authorize.InvalidRequestError;

/**
 * Exception levée quand on a une requête "authorize" incorrecte.
 * @author Lionel HERVIER
 */
public class AuthInvalidRequestException extends BaseAuthException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 6995063301241640216L;

	/**
	 * Constructeur
	 */
	public AuthInvalidRequestException(String message, String redirectUri) {
		super(message, redirectUri, new InvalidRequestError());
	}
	
}
