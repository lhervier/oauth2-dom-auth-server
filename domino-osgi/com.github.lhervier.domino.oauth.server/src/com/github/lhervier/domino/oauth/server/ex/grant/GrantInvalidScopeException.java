package com.github.lhervier.domino.oauth.server.ex.grant;

import com.github.lhervier.domino.oauth.server.ex.BaseGrantException;
import com.github.lhervier.domino.oauth.server.model.error.grant.InvalidScopeError;

public class GrantInvalidScopeException extends BaseGrantException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 3494299160598990258L;

	/**
	 * Constructeur
	 * @param message
	 */
	public GrantInvalidScopeException(String message) {
		super(message, new InvalidScopeError());
	}
}
