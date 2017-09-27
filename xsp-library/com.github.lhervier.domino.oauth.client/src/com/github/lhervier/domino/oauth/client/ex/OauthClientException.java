package com.github.lhervier.domino.oauth.client.ex;

public class OauthClientException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -6019350761545118817L;

	/**
	 * The object
	 */
	private Object error;
	
	/**
	 * Constructor
	 * @param message
	 */
	public OauthClientException(Object error) {
		super();
		this.error = error;
	}

	/**
	 * Constructor
	 */
	public OauthClientException(Object error, Throwable cause) {
		super(cause);
		this.error = error;
	}

	/**
	 * @return the error
	 */
	public Object getError() {
		return error;
	}
	
}
