package com.github.lhervier.domino.oauth.server.testsuite;

import com.github.lhervier.domino.oauth.server.services.TimeService;

public class TimeServiceTestImpl implements TimeService {

	public static long CURRENT_TIME = System.currentTimeMillis() / 1000L;

	@Override
	public long currentTimeSeconds() {
		return CURRENT_TIME;
	}
	
	
}
