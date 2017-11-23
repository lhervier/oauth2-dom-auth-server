package com.github.lhervier.domino.oauth.server.services.impl;

import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.services.TimeService;

@Service
public class TimeServiceImpl implements TimeService {

	/**
	 * @see com.github.lhervier.domino.oauth.server.services.TimeService#currentTimeSeconds()
	 */
	@Override
	public long currentTimeSeconds() {
		return System.currentTimeMillis() / 1000L;
	}

}
