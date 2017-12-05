package com.github.lhervier.domino.oauth.server.testsuite.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import com.github.lhervier.domino.oauth.server.NotesPrincipal.AuthType;
import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.repo.ApplicationRepository;
import com.github.lhervier.domino.oauth.server.services.AuthCodeService;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.controller.TestAuthCodeGrant.TokenResponse;
import com.github.lhervier.domino.oauth.server.testsuite.impl.DummyExtWithGrant;
import com.github.lhervier.domino.oauth.server.testsuite.impl.DummyExtWithGrantContext;
import com.github.lhervier.domino.oauth.server.testsuite.impl.NotesPrincipalTestImpl;

@SuppressWarnings("serial")
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
	public void expiredRefreshToken() throws Exception {
		when(authCodeSvcMock.toEntity(Mockito.eq("AZERTY"))).thenReturn(new AuthCodeEntity() {{
			setId("012345");
			setExpires(timeSvcStub.currentTimeSeconds() - 10L);		// Expired 10s ago
			setScopes(new ArrayList<String>());
			setGrantedScopes(new ArrayList<String>());
			setContextClasses(new HashMap<String, String>() {{
				put(DummyExtWithGrant.DUMMY_RESPONSE_TYPE, DummyExtWithGrantContext.class.getName());
			}});
			setContextObjects(new HashMap<String, String>() {{
				put(DummyExtWithGrant.DUMMY_RESPONSE_TYPE, mapper.writeValueAsString(new DummyExtWithGrantContext() {{
					setName("CN=Lionel/O=USER");
				}}));
			}});
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
	public void onlyAllowSubsetOfGrantedScopes() throws Exception {
		AuthCodeEntity code = new AuthCodeEntity() {{
			setId("012345");
			setClientId("1234");
			setExpires(timeSvcStub.currentTimeSeconds() + 10L);
			setScopes(Arrays.asList("scope1", "scope2", "scope3"));
			setGrantedScopes(Arrays.asList("scope1", "scope2"));
			setContextClasses(new HashMap<String, String>() {{
				put(DummyExtWithGrant.DUMMY_RESPONSE_TYPE, DummyExtWithGrantContext.class.getName());
			}});
			setContextObjects(new HashMap<String, String>() {{
				put(DummyExtWithGrant.DUMMY_RESPONSE_TYPE, mapper.writeValueAsString(new DummyExtWithGrantContext() {{
					setName("CN=Lionel/O=USER");
				}}));
			}});
		}};
		when(authCodeSvcMock.toEntity(Mockito.eq("AZERTY"))).thenReturn(code);
		
		this.mockMvc.perform(
				post("/token")
				.param("grant_type", "refresh_token")
				.param("refresh_token", "AZERTY")
				.param("scope", "scope1 scope2 scope3")
		).andExpect(status().is(400))
		.andExpect(content().string(containsString("invalid scope")));
		
		MvcResult result = this.mockMvc.perform(
				post("/token")
				.param("grant_type", "refresh_token")
				.param("refresh_token", "AZERTY")
				.param("scope", "scope1")
		).andExpect(status().is(200))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		TokenResponse response = this.mapper.readValue(json, TokenResponse.class);
		assertThat(response.getScope(), nullValue());
	}
	
	/**
	 * No scope => grant previously granted scopes
	 */
	@Test
	public void scopesDefaultsToGrantedScopes() throws Exception {
		when(authCodeSvcMock.toEntity(Mockito.eq("AZERTY"))).thenReturn(new AuthCodeEntity() {{
			setId("012345");
			setApplication(APP_NAME);
			setClientId(APP_CLIENT_ID);
			setExpires(timeSvcStub.currentTimeSeconds() + 10L);
			setScopes(Arrays.asList("scope1", "scope2"));
			setGrantedScopes(Arrays.asList("scope1"));
			setContextClasses(new HashMap<String, String>() {{
				put(DummyExtWithGrant.DUMMY_RESPONSE_TYPE, DummyExtWithGrantContext.class.getName());
			}});
			setContextObjects(new HashMap<String, String>() {{
				put(DummyExtWithGrant.DUMMY_RESPONSE_TYPE, mapper.writeValueAsString(new DummyExtWithGrantContext() {{
					setName("CN=Lionel/O=USER");
				}}));
			}});
		}});
		
		when(authCodeSvcMock.fromEntity(Mockito.any(AuthCodeEntity.class))).thenReturn("QSDFGH");
		
		MvcResult result = this.mockMvc.perform(
				post("/token")
				.param("grant_type", "refresh_token")
				.param("refresh_token", "AZERTY")
		).andExpect(status().is(200))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		TokenResponse response = this.mapper.readValue(json, TokenResponse.class);
		assertThat(response.getRefreshToken(), equalTo("QSDFGH"));
	}
	
	/**
	 * Refresh token generated for another app
	 */
	@Test
	public void generatedForAnotherApp() throws Exception {
		when(authCodeSvcMock.toEntity(Mockito.eq("AZERTY"))).thenReturn(new AuthCodeEntity() {{
			setId("012345");
			setApplication("otherApp");
			setClientId("5678");
			setExpires(timeSvcStub.currentTimeSeconds() + 10L);
			setScopes(new ArrayList<String>());
			setGrantedScopes(new ArrayList<String>());
			setContextClasses(new HashMap<String, String>() {{
				put(DummyExtWithGrant.DUMMY_RESPONSE_TYPE, DummyExtWithGrantContext.class.getName());
			}});
			setContextObjects(new HashMap<String, String>() {{
				put(DummyExtWithGrant.DUMMY_RESPONSE_TYPE, mapper.writeValueAsString(new DummyExtWithGrantContext() {{
					setName("CN=Lionel/O=USER");
				}}));
			}});
		}});
		
		this.mockMvc.perform(
				post("/token")
				.param("grant_type", "refresh_token")
				.param("refresh_token", "AZERTY")
		).andExpect(status().is(400))
		.andExpect(content().string(containsString("invalid client_id")));
	}
}
