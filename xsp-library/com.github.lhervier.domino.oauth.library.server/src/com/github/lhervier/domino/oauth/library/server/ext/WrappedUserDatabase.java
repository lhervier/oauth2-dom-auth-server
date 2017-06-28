package com.github.lhervier.domino.oauth.library.server.ext;

import lotus.domino.Database;

import com.ibm.domino.osgi.core.context.ContextInfo;

public class WrappedUserDatabase extends BaseWrappedDatabase {

	@Override
	public Database getDatabase() {
		return ContextInfo.getUserDatabase();
	}
}
