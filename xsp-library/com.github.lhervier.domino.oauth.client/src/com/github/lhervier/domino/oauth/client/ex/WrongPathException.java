package com.github.lhervier.domino.oauth.client.ex;

/**
 * Raised when trying to acces one of the controller
 * method when not on a oauth2-client configured database.
 * Raised from the aspect.
 * @author Lionel HERVIER
 */
public class WrongPathException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -1418317193872062742L;

}
