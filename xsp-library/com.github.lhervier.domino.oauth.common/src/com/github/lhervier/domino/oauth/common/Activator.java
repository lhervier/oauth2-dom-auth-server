package com.github.lhervier.domino.oauth.common;

import com.github.lhervier.domino.oauth.common.spring.SpringActivator;

public class Activator extends SpringActivator {

	/**
	 * Constructor
	 */
	public Activator() {
		super();
		this.addConfig(SpringConfig.class);
	}
}
