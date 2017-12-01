package com.github.lhervier.domino.oauth.server.testsuite.impl;

import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;

public class DummyExtContext {

	private AuthCodeEntity code;

	public AuthCodeEntity getCode() {
		return code;
	}

	public void setCode(AuthCodeEntity code) {
		this.code = code;
	}
}
