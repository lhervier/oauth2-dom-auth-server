package com.github.lhervier.domino.oauth.server.ex;

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

	public WrongPathException() {
		super();
	}

	public WrongPathException(String arg0) {
		super(arg0);
	}

	public WrongPathException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public WrongPathException(Throwable arg0) {
		super(arg0);
	}

}
