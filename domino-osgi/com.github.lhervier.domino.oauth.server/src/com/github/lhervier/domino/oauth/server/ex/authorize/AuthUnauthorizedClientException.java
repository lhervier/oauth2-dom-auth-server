package com.github.lhervier.domino.oauth.server.ex.authorize;

import com.github.lhervier.domino.oauth.server.ex.BaseAuthException;
import com.github.lhervier.domino.oauth.server.model.error.authorize.UnauthorizedClientError;

public class AuthUnauthorizedClientException extends BaseAuthException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -1959919623322631668L;

	/**
	 * Constructeur
	 */
	public AuthUnauthorizedClientException(String message, String redirectUri) {
		super(message, redirectUri, new UnauthorizedClientError());
	}
}
