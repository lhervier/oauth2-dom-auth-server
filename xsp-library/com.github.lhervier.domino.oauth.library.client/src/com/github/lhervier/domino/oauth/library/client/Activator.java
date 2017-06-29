package com.github.lhervier.domino.oauth.library.client;

import com.github.lhervier.domino.oauth.common.spring.SpringActivator;

public class Activator extends SpringActivator {
    public static final String PLUGIN_ID = Activator.class.getPackage().getName();
    
    /**
	 * The instance
	 */
	private static Activator instance;
	public static Activator getDefault() {
		return instance;
	}
	
    public Activator() {
    	super(com.github.lhervier.domino.oauth.common.Activator.class);
    }
}
