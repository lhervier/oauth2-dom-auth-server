package com.github.lhervier.domino.oauth.server.model.error.grant;


public class ServerError extends GrantError {

	/**
	 * Constructeur
	 */
	public ServerError() {
		this.setError("server_error");
		this.setErrorUri("http://lhervier.github.com/dom-auth-server/error/grant/server_error");
	}
}
