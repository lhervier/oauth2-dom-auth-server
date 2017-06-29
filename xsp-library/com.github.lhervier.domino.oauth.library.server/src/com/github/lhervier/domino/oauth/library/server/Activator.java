package com.github.lhervier.domino.oauth.library.server;

import com.github.lhervier.domino.oauth.common.spring.SpringActivator;

public class Activator extends SpringActivator {
	public static final String PLUGIN_ID = Activator.class.getPackage().getName();
	public static final String SCOPE_EXT_ID = "com.github.lhervier.domino.oauth.library.server.scope";
	
	public Activator() {
		super(ServerConfig.class);
	}
}
