package com.github.lhervier.domino.oauth.server.testsuite.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import com.github.lhervier.domino.oauth.server.NotesPrincipal.AuthType;
import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.impl.NotesPrincipalTestImpl;

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
	public void whenNoRefreshToken_then400() throws Exception {
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
	public void whenInvalidRefreshToken_then400() throws Exception {
		this.mockMvc.perform(
				post("/token")
				.param("grant_type", "refresh_token")
				.param("refresh_token", "AZERTY")
		).andExpect(status().is(400))
		.andExpect(content().string(containsString("invalid refresh_token")));
	}
	
	/**
	 * expired refresh token
	 */
	@Test
	public void whenExpiredRefreshToken_then400() throws Exception {
		AuthCodeEntity entity = new AuthCodeEntity() {{
			setId("012345");
			setExpires(timeSvc.currentTimeSeconds() - 10L);		// Expired 10s ago
			setScopes(new ArrayList<String>());
			setGrantedScopes(new ArrayList<String>());
		}};
		String refreshToken = this.jwtSvc.createJwe(entity, "xx");
		
		this.mockMvc.perform(
				post("/token")
				.param("grant_type", "refresh_token")
				.param("refresh_token", refreshToken)
		).andExpect(status().is(400))
		.andExpect(content().string(containsString("invalid refresh_token")));
	}
	
	/**
	 * Only allow subset of already granted scopes 
	 */
	@Test
	public void whenAskedForSubsetOfGrantedScopes_thenOK() throws Exception {
		AuthCodeEntity code = new AuthCodeEntity() {{
			setId("012345");
			setClientId("1234");
			setExpires(timeSvc.currentTimeSeconds() + 10L);
			setScopes(Arrays.asList("scope1", "scope2", "scope3"));
			setGrantedScopes(Arrays.asList("scope1", "scope2"));
		}};
		String refreshToken = this.jwtSvc.createJwe(code, "xx");
		
		MvcResult result = this.mockMvc.perform(
				post("/token")
				.param("grant_type", "refresh_token")
				.param("refresh_token", refreshToken)
				.param("scope", "scope1")
		).andExpect(status().is(200))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		Map<String, Object> response = this.fromJson(json);
		assertThat(response.get("scope"), nullValue());
	}
	
	/**
	 * Only allow subset of already granted scopes 
	 */
	@Test
	public void whenNotAskedForSubsetOfGrantedScopes_then400() throws Exception {
		AuthCodeEntity code = new AuthCodeEntity() {{
			setId("012345");
			setClientId("1234");
			setExpires(timeSvc.currentTimeSeconds() + 10L);
			setScopes(Arrays.asList("scope1", "scope2", "scope3"));
			setGrantedScopes(Arrays.asList("scope1", "scope2"));
		}};
		String refreshToken = this.jwtSvc.createJwe(code, "xx");
		
		this.mockMvc.perform(
				post("/token")
				.param("grant_type", "refresh_token")
				.param("refresh_token", refreshToken)
				.param("scope", "scope1 scope2 scope3")
		).andExpect(status().is(400))
		.andExpect(content().string(containsString("invalid scope")));
	}
	
	/**
	 * No scope => grant previously granted scopes
	 */
	@Test
	public void whenNoScope_thenGrantedScopesRemainsTheSame() throws Exception {
		AuthCodeEntity code = new AuthCodeEntity() {{
			setId("012345");
			setApplication(APP_NAME);
			setClientId(APP_CLIENT_ID);
			setExpires(timeSvc.currentTimeSeconds() + 10L);
			setScopes(Arrays.asList("scope1", "scope2"));
			setGrantedScopes(Arrays.asList("scope1"));
		}};
		String refreshToken = this.jwtSvc.createJwe(code, "xx");
		
		MvcResult result = this.mockMvc.perform(
				post("/token")
				.param("grant_type", "refresh_token")
				.param("refresh_token", refreshToken)
		).andExpect(status().is(200))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		Map<String, Object> response = this.fromJson(json);
		assertThat(response.get("refresh_token"), CoreMatchers.notNullValue());
		
		AuthCodeEntity newCode = this.jwtSvc.fromJwe(response.get("refresh_token").toString(), "xx", AuthCodeEntity.class);
		assertThat(newCode.getGrantedScopes(), containsInAnyOrder("scope1"));
	}
	
	/**
	 * Refresh token generated for another app
	 */
	@Test
	public void whenRefreshTokenGeneratedForAnotherApp_then400() throws Exception {
		AuthCodeEntity code = new AuthCodeEntity() {{
			setId("012345");
			setApplication("otherApp");
			setClientId("5678");
			setExpires(timeSvc.currentTimeSeconds() + 10L);
			setScopes(new ArrayList<String>());
			setGrantedScopes(new ArrayList<String>());
		}};
		String refreshToken = this.jwtSvc.createJwe(code, "xx");
		
		this.mockMvc.perform(
				post("/token")
				.param("grant_type", "refresh_token")
				.param("refresh_token", refreshToken)
		).andExpect(status().is(400))
		.andExpect(content().string(containsString("invalid client_id")));
	}
}
