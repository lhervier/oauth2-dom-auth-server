package com.github.lhervier.domino.oauth.server.testsuite.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.ex.BaseGrantException;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.services.GrantService;

/**
 * Dummy grant to test TokenController
 * @author Lionel HERVIER
 */
@Service("dummy_grant")
public class DummyGrantServiceImpl implements GrantService {

	/**
	 * @see com.github.lhervier.domino.oauth.server.services.GrantService#createGrant(com.github.lhervier.domino.oauth.server.model.Application, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Map<String, Object> createGrant(Application app) throws BaseGrantException {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("token", "12345");
		return ret;
	}

}
