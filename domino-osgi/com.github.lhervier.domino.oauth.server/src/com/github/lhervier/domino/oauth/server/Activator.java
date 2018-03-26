package com.github.lhervier.domino.oauth.server;

import org.eclipse.core.runtime.Plugin;

public class Activator extends Plugin {
	public static final String PLUGIN_ID = Activator.class.getPackage().getName();
	public static final String SCOPE_EXT_ID = "com.github.lhervier.domino.oauth.server.scope";
	
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
		instance = this;	// NOSONAR
	}
	
}
