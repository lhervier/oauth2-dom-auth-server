package com.github.lhervier.domino.oauth.server.utils;

public class SystemUtils {

	/**
	 * Retourne la date courante en epoch seconds
	 * @return la date courante en epoch seconds
	 */
	public static final long currentTimeSeconds() {
		return System.currentTimeMillis() / 1000L;
	}
}
