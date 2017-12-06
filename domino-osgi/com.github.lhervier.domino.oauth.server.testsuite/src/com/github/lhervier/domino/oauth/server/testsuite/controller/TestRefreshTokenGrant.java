package com.github.lhervier.domino.oauth.server.testsuite.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import com.github.lhervier.domino.oauth.server.NotesPrincipal.AuthType;
import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.repo.ApplicationRepository;
import com.github.lhervier.domino.oauth.server.services.AuthCodeService;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.controller.TestAuthCodeGrant.TkResp;
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
	 * App repo mock
	 */
	@Autowired
	private ApplicationRepository appRepoMock;
	
	/**
	 * The auth code service
	 */
	@Autowired
	private AuthCodeService authCodeSvcMock;
	
	/**
	 * User principal
	 */
	@Autowired
	protected NotesPrincipalTestImpl user;
	
	@Before
	public void before() throws Exception {
		reset(appRepoMock);
		reset(authCodeSvcMock);
		
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
		when(authCodeSvcMock.toEntity(Mockito.eq("AZERTY"))).thenReturn(null);
		
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
		when(authCodeSvcMock.toEntity(Mockito.eq("AZERTY"))).thenReturn(new AuthCodeEntity() {{
			setId("012345");
			setExpires(timeSvcStub.currentTimeSeconds() - 10L);		// Expired 10s ago
			setScopes(new ArrayList<String>());
			setGrantedScopes(new ArrayList<String>());
		}});
		
		this.mockMvc.perform(
				post("/token")
				.param("grant_type", "refresh_token")
				.param("refresh_token", "AZERTY")
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
			setExpires(timeSvcStub.currentTimeSeconds() + 10L);
			setScopes(Arrays.asList("scope1", "scope2", "scope3"));
			setGrantedScopes(Arrays.asList("scope1", "scope2"));
		}};
		when(authCodeSvcMock.toEntity(Mockito.eq("AZERTY"))).thenReturn(code);
		
		MvcResult result = this.mockMvc.perform(
				post("/token")
				.param("grant_type", "refresh_token")
				.param("refresh_token", "AZERTY")
				.param("scope", "scope1")
		).andExpect(status().is(200))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		TkResp response = this.mapper.readValue(json, TkResp.class);
		assertThat(response.getScope(), nullValue());
	}
	
	/**
	 * Only allow subset of already granted scopes 
	 */
	@Test
	public void whenNotAskedForSubsetOfGrantedScopes_then400() throws Exception {
		AuthCodeEntity code = new AuthCodeEntity() {{
			setId("012345");
			setClientId("1234");
			setExpires(timeSvcStub.currentTimeSeconds() + 10L);
			setScopes(Arrays.asList("scope1", "scope2", "scope3"));
			setGrantedScopes(Arrays.asList("scope1", "scope2"));
		}};
		when(authCodeSvcMock.toEntity(Mockito.eq("AZERTY"))).thenReturn(code);
		
		this.mockMvc.perform(
				post("/token")
				.param("grant_type", "refresh_token")
				.param("refresh_token", "AZERTY")
				.param("scope", "scope1 scope2 scope3")
		).andExpect(status().is(400))
		.andExpect(content().string(containsString("invalid scope")));
	}
	
	/**
	 * No scope => grant previously granted scopes
	 */
	@Test
	public void whenNoScope_thenGrantedScopesRemainsTheSame() throws Exception {
		when(authCodeSvcMock.toEntity(Mockito.eq("AZERTY"))).thenReturn(new AuthCodeEntity() {{
			setId("012345");
			setApplication(APP_NAME);
			setClientId(APP_CLIENT_ID);
			setExpires(timeSvcStub.currentTimeSeconds() + 10L);
			setScopes(Arrays.asList("scope1", "scope2"));
			setGrantedScopes(Arrays.asList("scope1"));
		}});
		
		when(authCodeSvcMock.fromEntity(Mockito.any(AuthCodeEntity.class))).thenReturn("QSDFGH");
		
		MvcResult result = this.mockMvc.perform(
				post("/token")
				.param("grant_type", "refresh_token")
				.param("refresh_token", "AZERTY")
		).andExpect(status().is(200))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		TkResp response = this.mapper.readValue(json, TkResp.class);
		assertThat(response.getRefreshToken(), equalTo("QSDFGH"));
		
		ArgumentCaptor<AuthCodeEntity> captor = ArgumentCaptor.forClass(AuthCodeEntity.class);
		verify(authCodeSvcMock, times(1)).fromEntity(captor.capture());
		List<AuthCodeEntity> values = captor.getAllValues();
		assertThat(values.size(), equalTo(1));
		AuthCodeEntity code = values.get(0);
		assertThat(code.getGrantedScopes(), containsInAnyOrder("scope1"));
	}
	
	/**
	 * Refresh token generated for another app
	 */
	@Test
	public void whenRefreshTokenGeneratedForAnotherApp_then400() throws Exception {
		when(authCodeSvcMock.toEntity(Mockito.eq("AZERTY"))).thenReturn(new AuthCodeEntity() {{
			setId("012345");
			setApplication("otherApp");
			setClientId("5678");
			setExpires(timeSvcStub.currentTimeSeconds() + 10L);
			setScopes(new ArrayList<String>());
			setGrantedScopes(new ArrayList<String>());
		}});
		
		this.mockMvc.perform(
				post("/token")
				.param("grant_type", "refresh_token")
				.param("refresh_token", "AZERTY")
		).andExpect(status().is(400))
		.andExpect(content().string(containsString("invalid client_id")));
	}
}
