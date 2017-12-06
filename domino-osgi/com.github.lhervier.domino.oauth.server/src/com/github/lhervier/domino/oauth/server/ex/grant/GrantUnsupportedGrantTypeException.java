package com.github.lhervier.domino.oauth.server.ex.grant;

import com.github.lhervier.domino.oauth.server.ex.BaseGrantException;
import com.github.lhervier.domino.oauth.server.model.error.grant.UnsupportedGrantTypeError;

public class GrantUnsupportedGrantTypeException extends BaseGrantException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 5770648849912500954L;

	/**
	 * Constructeur
	 * @param message
	 */
	public GrantUnsupportedGrantTypeException(String message) {
		super(message, new UnsupportedGrantTypeError());
	}
}
