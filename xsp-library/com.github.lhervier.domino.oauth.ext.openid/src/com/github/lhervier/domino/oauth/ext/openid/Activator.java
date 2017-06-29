package com.github.lhervier.domino.oauth.ext.openid;

import com.github.lhervier.domino.oauth.common.spring.SpringActivator;

public class Activator extends SpringActivator {
	public static final String PLUGIN_ID = Activator.class.getPackage().getName();
	
	public Activator() {
		super(UserInfoServlet.class);
	}
}
