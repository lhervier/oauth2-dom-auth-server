package com.github.lhervier.domino.oauth.client.ex;

import com.github.lhervier.domino.oauth.client.model.GrantError;

/**
 * Error while trying to refresh the token
 * @author Lionel HERVIER
 */
public class RefreshTokenException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -235912819397780656L;

	/**
	 * The grant error
	 */
	private GrantError error;
	
	/**
	 * Constructor
	 */
	public RefreshTokenException(GrantError e) {
		this.error = e;
	}

	public GrantError getError() {return error;}
	public void setError(GrantError error) {this.error = error;}
}
