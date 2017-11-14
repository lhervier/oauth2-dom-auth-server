package com.github.lhervier.domino.oauth.server.ex.authorize;

import com.github.lhervier.domino.oauth.server.ex.BaseAuthException;
import com.github.lhervier.domino.oauth.server.model.error.authorize.UnsupportedResponseTypeError;

public class AuthUnsupportedResponseTypeException extends BaseAuthException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -112334089500453291L;

	/**
	 * Constructeur
	 */
	public AuthUnsupportedResponseTypeException(String message, String redirectUri) {
		super(message, redirectUri, new UnsupportedResponseTypeError());
	}
}
