package com.github.lhervier.domino.oauth.common.spring.wrap;

import lotus.domino.Session;

import com.ibm.domino.osgi.core.context.ContextInfo;

public class WrappedUserSession extends BaseWrappedSession {

	@Override
	public Session getSession() {
		return ContextInfo.getUserSession();
	}

}