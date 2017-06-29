package com.github.lhervier.domino.oauth.common;

import com.github.lhervier.domino.oauth.common.spring.SpringActivator;
import com.github.lhervier.domino.oauth.common.spring.SpringServletConfig;

public class Activator extends SpringActivator {

	/**
	 * Constructor
	 */
	public Activator() {
		super();
		this.addConfig(SpringServletConfig.class);
	}
}
