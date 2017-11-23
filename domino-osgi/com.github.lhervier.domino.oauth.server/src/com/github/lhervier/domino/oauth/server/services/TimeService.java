package com.github.lhervier.domino.oauth.server.services;

/**
 * Service to acces system current time
 * @author Lionel HERVIER
 */
public interface TimeService {

	/**
	 * Returns the current time in epoch seconds
	 */
	public long currentTimeSeconds();
}
