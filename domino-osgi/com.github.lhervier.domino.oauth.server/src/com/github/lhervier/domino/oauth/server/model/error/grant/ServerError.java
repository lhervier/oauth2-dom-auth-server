package com.github.lhervier.domino.oauth.server.model.error.grant;


public class ServerError extends GrantError {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -4198715094515460320L;

	/**
	 * Constructeur
	 */
	public ServerError() {
		this.setError("server_error");
		this.setErrorUri("http://lhervier.github.com/dom-auth-server/error/grant/server_error");
	}
}
