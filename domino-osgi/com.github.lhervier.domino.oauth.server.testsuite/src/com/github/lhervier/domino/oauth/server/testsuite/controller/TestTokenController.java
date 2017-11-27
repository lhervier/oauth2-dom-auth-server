package com.github.lhervier.domino.oauth.server.testsuite.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.codehaus.jackson.annotate.JsonProperty;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MvcResult;

import com.github.lhervier.domino.oauth.server.NotesPrincipal.AuthType;
import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ext.core.CoreContext;
import com.github.lhervier.domino.oauth.server.ext.core.CoreExt;
import com.github.lhervier.domino.oauth.server.repo.ApplicationRepository;
import com.github.lhervier.domino.oauth.server.repo.AuthCodeRepository;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.NotesPrincipalTestImpl;
import com.github.lhervier.domino.oauth.server.testsuite.TestConfig;
import com.github.lhervier.domino.oauth.server.testsuite.TimeServiceTestImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("test")
@SuppressWarnings("serial")
public class TestTokenController extends BaseTest {

	/**
	 * "normal" application.
	 */
	private static final String APP_NAME = "myApp";
	private static final String APP_FULL_NAME = "CN=myApp/OU=APPLICATION/O=WEB";
	private static final String APP_CLIENT_ID = "1234";
	private static final String APP_REDIRECT_URI = "http://acme.com/myApp";
	
	/**
	 * Hacky app: Try to declare another app that points to the same redirect uri
	 */
	private static final String HACKY_APP_NAME = "hackyApp";
	private static final String HACKY_APP_FULL_NAME = "CN=hackyApp/OU=APPLICATION/O=WEB";
	private static final String HACKY_APP_CLIENT_ID = "3456";
	private static final String HACKY_APP_REDIRECT_URI = "http://acme.com/myApp";
	
	/**
	 * Authorization codes
	 */
	private static final String AUTH_CODE_ID = "012345";								// for normal app, as Lionel
	
	/**
	 * App repo mock
	 */
	@Autowired
	private ApplicationRepository appRepoMock;
	
	/**
	 * Auth code repo mock
	 */
	@Autowired
	private AuthCodeRepository authCodeRepoMock;
	
	/**
	 * Core extension
	 */
	@Autowired
	private CoreExt coreExt;
	
	/**
	 * User principal
	 */
	@Autowired
	protected NotesPrincipalTestImpl user;
	
	/**
	 * Before
	 */
	@Before
	public void before() throws IOException {
		// Reset repositories
		reset(appRepoMock);
		reset(authCodeRepoMock);
		
		// Declare applications
		ApplicationEntity normalApp = new ApplicationEntity() {{
			this.setClientId(APP_CLIENT_ID);
			this.setFullName(APP_FULL_NAME);
			this.setName(APP_NAME);
			this.setRedirectUri(APP_REDIRECT_URI);
		}};
		when(this.appRepoMock.findOne(eq(APP_CLIENT_ID))).thenReturn(normalApp);
		when(this.appRepoMock.findOneByName(eq(APP_NAME))).thenReturn(normalApp);
		
		ApplicationEntity hackyApp = new ApplicationEntity() {{
			this.setClientId(HACKY_APP_CLIENT_ID);
			this.setFullName(HACKY_APP_FULL_NAME);
			this.setName(HACKY_APP_NAME);
			this.setRedirectUri(HACKY_APP_REDIRECT_URI);
		}};
		when(this.appRepoMock.findOne(eq(HACKY_APP_CLIENT_ID))).thenReturn(hackyApp);
		when(this.appRepoMock.findOneByName(eq(HACKY_APP_NAME))).thenReturn(hackyApp);
		
		// Declare authorization codes
		AuthCodeEntity code = new AuthCodeEntity() {{
			this.setId(AUTH_CODE_ID);
			this.setApplication(APP_NAME);
			this.setClientId(APP_CLIENT_ID);
			this.setExpires(TimeServiceTestImpl.CURRENT_TIME + 600);
			this.setScopes(Arrays.asList("scope1", "scope2"));
			this.setGrantedScopes(new ArrayList<String>());
			this.setRedirectUri(APP_REDIRECT_URI);
			this.setContextClasses(new HashMap<String, String>() {{
				put(
						coreExt.getId(), 
						CoreContext.class.getName()
				);
			}});
			this.setContextObjects(new HashMap<String, String>() {{
				put(
						coreExt.getId(),
						mapper.writeValueAsString(new CoreContext() {{
							this.setAud(APP_CLIENT_ID);
							this.setSub("CN=Lionel/o=USER");
							this.setIss("https://acme.com/domino/oauth2/");
						}})
				);
			}});
		}};
		when(this.authCodeRepoMock.findOne(eq(AUTH_CODE_ID))).thenReturn(code);
		
		// Login as normal application.
		user.setAuthType(AuthType.NOTES);
		user.setName(APP_FULL_NAME);
		user.setCommon(APP_NAME);
		user.setRoles(new ArrayList<String>());
		user.setClientId(null);
		user.setScopes(null);
		user.setCurrentDatabasePath(this.oauth2Db);
	}
	
	public static class TokenResponse {
		@JsonProperty("refresh_token")
		private String refreshToken;
		@JsonProperty("access_token")
		private String accessToken;
		@JsonProperty("id_token")
		private String idToken;
		@JsonProperty("expires_in")
		private long expiresIn;
		@JsonProperty("token_type")
		private String tokenType;
		private String scope;
		public String getRefreshToken() { return refreshToken; }
		public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
		public String getAccessToken() { return accessToken; }
		public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
		public String getIdToken() { return idToken; }
		public void setIdToken(String idToken) { this.idToken = idToken; }
		public long getExpiresIn() { return expiresIn; }
		public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
		public String getTokenType() { return tokenType; }
		public void setTokenType(String tokenType) { this.tokenType = tokenType; }
		public String getScope() { return scope; }
		public void setScope(String scope) { this.scope = scope; }
	};
	
	// ======================================================================
	
	/**
	 * Get tokens from auth code using GET is forbidden
	 */
	@Test
	public void postMandatory() throws Exception {
		this.mockMvc.perform(
				get("/token")
				.param("grant_type", "authorization_code")
				.param("code", AUTH_CODE_ID)
		).andExpect(status().is(500))		// RFC is not specifying that we must return a 400 error here. 500 is spring default behaviour and we don't want to change it...
		.andExpect(content().string(CoreMatchers.containsString("Request method 'GET' not supported")));
	}
	
	/**
	 * Code is mandatory
	 */
	@Test
	public void codeMandatory() throws Exception {
		this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("code", "")
		).andExpect(status().is(400))
		.andExpect(content().string(containsString("code is mandatory")));
	}
	
	/**
	 * Redirect uri must be the same as the one asked in authorize request
	 */
	@Test
	public void wrongRedirectUri() throws Exception {
		this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("code", AUTH_CODE_ID)
				.param("redirect_uri", "http://wrong.redirect/uri")
		).andExpect(status().is(400))
		.andExpect(content().string(containsString("invalid redirect_uri")));
	}
	
	/**
	 * Expires auth code
	 */
	@Test
	public void expiredAuthCode() throws Exception {
		TimeServiceTestImpl.CURRENT_TIME = (System.currentTimeMillis() / 1000L) + 700L;
		
		this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("code", AUTH_CODE_ID)
				.param("redirect_uri", APP_REDIRECT_URI)
		).andExpect(status().is(400))
		.andExpect(content().string(containsString("code has expired")));
	}
	
	/**
	 * Auth code must have been generated for the
	 * currently authenticated app
	 */
	@Test
	public void wrongClientId() throws Exception {
		// Login as hacky app
		user.setName(HACKY_APP_FULL_NAME);
		user.setCommon(HACKY_APP_NAME);
		
		// Try to exchange the auth code from normal app
		this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("code", AUTH_CODE_ID)
				.param("redirect_uri", APP_REDIRECT_URI)
		).andExpect(status().is(400))
		.andExpect(content().string(containsString("code generated for another app")));
	}
	
	/**
	 * Get tokens from auth code
	 */
	@Test
	public void tokensFromAuthCode() throws Exception {
		MvcResult result = this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("redirect_uri", APP_REDIRECT_URI)
				.param("code", AUTH_CODE_ID)
		).andExpect(status().is(200))
		.andExpect(content().contentType("application/json;charset=UTF-8"))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		
		TokenResponse resp = this.mapper.readValue(json, TokenResponse.class);
		assertThat(resp.getRefreshToken(), is(notNullValue()));
		assertThat(resp.getAccessToken(), is(notNullValue()));
		assertThat(resp.getTokenType(), is("Bearer"));
		assertThat(resp.getScope(), is(""));
		assertThat(resp.getExpiresIn(), is(equalTo(36000L)));
		
		assertThat(resp.getIdToken(), is(nullValue()));
	}
}
