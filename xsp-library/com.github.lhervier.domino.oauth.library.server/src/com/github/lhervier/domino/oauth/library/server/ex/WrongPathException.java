package com.github.lhervier.domino.oauth.library.server.ex;

/**
 * Exception raised when trying to access endpoints into
 * another database than the one declared in the properties
 * (in the notes.ini)
 * @author Lionel HERVIER
 */
public class WrongPathException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 1L;

}
