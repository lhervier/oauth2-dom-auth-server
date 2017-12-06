package com.github.lhervier.domino.oauth.server.ex;

/**
 * Exception raised when trying to access endpoints into
 * another database than the one declared in the properties
 * (in the notes.ini)
 * @author Lionel HERVIER
 */
public class WrongPathException extends Exception {

	private static final long serialVersionUID = 1L;

	public WrongPathException(String message) {
		super(message);
	}
}
