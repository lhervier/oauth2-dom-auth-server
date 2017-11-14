package com.github.lhervier.domino.oauth.server.ex;

public class ForbiddenException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -6672740610682450417L;

	public ForbiddenException() {
		super("forbidden");
	}
}
