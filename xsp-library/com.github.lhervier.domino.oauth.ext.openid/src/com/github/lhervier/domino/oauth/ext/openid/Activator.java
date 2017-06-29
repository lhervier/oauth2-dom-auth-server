package com.github.lhervier.domino.oauth.ext.openid;

public class Activator extends com.github.lhervier.domino.oauth.library.server.Activator {
	public static final String PLUGIN_ID = Activator.class.getPackage().getName();
	
	public Activator() {
		super();
		this.addConfig(OpenIDConfig.class);
	}
}
