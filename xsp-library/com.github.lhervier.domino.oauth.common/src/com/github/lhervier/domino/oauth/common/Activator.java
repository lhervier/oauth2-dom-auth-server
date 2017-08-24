package com.github.lhervier.domino.oauth.common;

import org.eclipse.core.runtime.Plugin;

public class Activator extends Plugin {

	/**
	 * The instance
	 */
	private static Activator instance;
	public static Activator getDefault() {
		return instance;
	}
	
	/**
	 * Constructor
	 */
	public Activator() {
		instance = this;
	}
	
}
