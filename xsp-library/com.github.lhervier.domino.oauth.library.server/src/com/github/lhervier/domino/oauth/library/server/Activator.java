package com.github.lhervier.domino.oauth.library.server;


public class Activator extends com.github.lhervier.domino.oauth.common.Activator {
	public static final String PLUGIN_ID = Activator.class.getPackage().getName();
	public static final String SCOPE_EXT_ID = "com.github.lhervier.domino.oauth.library.server.scope";
	
	/**
	 * Constructor
	 */
	public Activator() {
		super();
		this.addConfig(ServerConfig.class);
	}
	
}
