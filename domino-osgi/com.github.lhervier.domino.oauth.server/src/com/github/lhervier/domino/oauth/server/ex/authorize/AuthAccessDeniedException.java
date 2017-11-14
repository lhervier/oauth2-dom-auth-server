package com.github.lhervier.domino.oauth.server.ex.authorize;

import com.github.lhervier.domino.oauth.server.ex.BaseAuthException;
import com.github.lhervier.domino.oauth.server.model.error.authorize.AccessDeniedError;

public class AuthAccessDeniedException extends BaseAuthException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 8016993714664739993L;

	/**
	 * Constructeur
	 */
	public AuthAccessDeniedException(String message, String redirectUri) {
		super(message, redirectUri, new AccessDeniedError());
	}

}
