package com.github.lhervier.domino.oauth.ext.openid;

import org.eclipse.core.runtime.Plugin;

public class Activator extends Plugin {
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
		instance = this;
	}
}
