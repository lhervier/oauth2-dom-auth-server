package com.github.lhervier.domino.oauth.common.spring.wrap;

import lotus.domino.Database;

import com.ibm.domino.osgi.core.context.ContextInfo;

public class WrappedUserDatabase extends BaseWrappedDatabase {

	@Override
	public Database getDatabase() {
		return ContextInfo.getUserDatabase();
	}
}
