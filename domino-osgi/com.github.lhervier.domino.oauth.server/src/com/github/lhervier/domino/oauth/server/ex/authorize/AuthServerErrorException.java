package com.github.lhervier.domino.oauth.server.ex.authorize;

import com.github.lhervier.domino.oauth.server.ex.BaseAuthException;
import com.github.lhervier.domino.oauth.server.model.error.authorize.ServerError;

public class AuthServerErrorException extends BaseAuthException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 3698177808182780992L;

	/**
	 * Constructeur
	 */
	public AuthServerErrorException(String message, String redirectUri) {
		super(message, redirectUri, new ServerError());
	}

}
