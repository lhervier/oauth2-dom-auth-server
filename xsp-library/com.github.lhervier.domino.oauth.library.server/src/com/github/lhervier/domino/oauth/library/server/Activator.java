package com.github.lhervier.domino.oauth.library.server;

import com.github.lhervier.domino.oauth.common.spring.SpringActivator;

public class Activator extends SpringActivator {
	public static final String PLUGIN_ID = Activator.class.getPackage().getName();
	public static final String SCOPE_EXT_ID = "com.github.lhervier.domino.oauth.library.server.scope";
	
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
		super(com.github.lhervier.domino.oauth.common.Activator.class);
		this.addConfig(ServerConfig.class);
		instance = this;
	}
	
}
