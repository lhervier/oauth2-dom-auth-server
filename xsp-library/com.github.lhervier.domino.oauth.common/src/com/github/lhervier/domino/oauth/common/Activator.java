package com.github.lhervier.domino.oauth.common;

import com.github.lhervier.domino.oauth.common.spring.SpringActivator;

public class Activator extends SpringActivator {

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
		super(null);
		instance = this;
		this.addConfig(SpringConfig.class);
	}
	
	
}
