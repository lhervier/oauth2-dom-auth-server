package com.github.lhervier.domino.oauth.server.testsuite.ext.openid;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.lhervier.domino.oauth.server.ext.openid.OpenIDExt;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;

public class TestOpenIdExt extends BaseTest {

	@Autowired private OpenIDExt openIdExt;
	
	/**
	 * Id is "openid"... even if we don't care...
	 */
	@Test
	public void idIsOpenId() throws Exception {
		assertThat(openIdExt.getId(), equalTo("openid"));
	}
	
	/**
	 * Only used when response_type "id_token"
	 */
	@Test
	public void responseTypeMustContainIdToken() throws Exception {
		assertThat(
				openIdExt.validateResponseTypes(Arrays.asList("code", "id_token")),
				equalTo(true)
		);
		
		assertThat(
				openIdExt.validateResponseTypes(Arrays.asList("code")),
				equalTo(false)
		);
	}
	
	/**
	 * Check that nonce parameter is forwared
	 */
	
}
