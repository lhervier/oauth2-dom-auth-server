package com.github.lhervier.domino.oauth.server.testsuite.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.lhervier.domino.oauth.server.NotesPrincipal.AuthType;
import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.repo.ApplicationRepository;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.NotesPrincipalTestImpl;

public class TestRefreshTokenGrant extends BaseTest {

	/**
	 * "normal" application.
	 */
	private static final String APP_NAME = "myApp";
	private static final String APP_FULL_NAME = "CN=myApp/OU=APPLICATION/O=WEB";
	private static final String APP_CLIENT_ID = "1234";
	private static final String APP_REDIRECT_URI = "http://acme.com/myApp";
	private ApplicationEntity normalApp;
	
	/**
	 * App repo mock
	 */
	@Autowired
	private ApplicationRepository appRepoMock;
	
	/**
	 * User principal
	 */
	@Autowired
	protected NotesPrincipalTestImpl user;
	
	@Before
	public void before() throws Exception {
		this.normalApp = new ApplicationEntity() {{
			this.setClientId(APP_CLIENT_ID);
			this.setFullName(APP_FULL_NAME);
			this.setName(APP_NAME);
			this.setRedirectUri(APP_REDIRECT_URI);
		}};
		when(this.appRepoMock.findOne(eq(APP_CLIENT_ID))).thenReturn(normalApp);
		when(this.appRepoMock.findOneByName(eq(APP_NAME))).thenReturn(normalApp);
		
		user.setAuthType(AuthType.NOTES);
		user.setName(APP_FULL_NAME);
		user.setCommon(APP_NAME);
		user.setRoles(new ArrayList<String>());
		user.setClientId(null);
		user.setScopes(null);
		user.setCurrentDatabasePath(this.oauth2Db);
	}
	
	/**
	 * refresh_token is mandatory
	 */
	@Test
	public void noRefreshToken() throws Exception {
		this.mockMvc.perform(
				post("/token")
				.param("grant_type", "refresh_token")
		).andExpect(status().is(400))
		.andExpect(content().string(containsString("refresh_token is mandatory")));
	}
	
	/**
	 * Invalid refresh_token
	 */
	@Test
	public void invalidRefreshToken() throws Exception {
		
	}
}
