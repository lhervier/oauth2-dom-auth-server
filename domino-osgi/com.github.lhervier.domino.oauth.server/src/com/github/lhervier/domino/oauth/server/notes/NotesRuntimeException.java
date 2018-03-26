package com.github.lhervier.domino.oauth.server.notes;

/**
 * Exception raised when a NotesException occured.
 * But this one is a RuntimeException, and don't have to be catched
 * @author Lionel HERVIER 
 */
public class NotesRuntimeException extends RuntimeException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 1514050954072534110L;

	/**
	 * Constructor
	 * @param message the message
	 */
	public NotesRuntimeException(String message) {
		super(message);
	}
	
	/**
	 * Constructor
	 * @param cause the cause
	 */
	public NotesRuntimeException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Constructor
	 * @param message the message
	 * @param cause the cause
	 */
	public NotesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
