package com.github.lhervier.domino.oauth.ext.openid;

import com.github.lhervier.domino.oauth.common.spring.SpringActivator;

public class Activator extends SpringActivator {
	public static final String PLUGIN_ID = Activator.class.getPackage().getName();
	
	/**
	 * The instance
	 */
	private static Activator instance;
	
	/**
	 * Mandatory to propagate Spring Contexts.
	 * @return the instance. 
	 */
	public static Activator getDefault() {
		return instance;
	}
	
	public Activator() {
		super(com.github.lhervier.domino.oauth.library.server.Activator.class);
		instance = this;
		this.addConfig(OpenIDConfig.class);
	}
}
