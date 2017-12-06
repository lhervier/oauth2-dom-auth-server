package com.github.lhervier.domino.oauth.server.testsuite.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.NotesPrincipal.AuthType;
import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ext.IAuthorizer;
import com.github.lhervier.domino.oauth.server.ext.IOAuthAuthorizeExtension;
import com.github.lhervier.domino.oauth.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.repo.ApplicationRepository;
import com.github.lhervier.domino.oauth.server.repo.AuthCodeRepository;
import com.github.lhervier.domino.oauth.server.services.AuthCodeService;
import com.github.lhervier.domino.oauth.server.services.ExtensionService;
import com.github.lhervier.domino.oauth.server.services.impl.AppServiceImpl;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.impl.DummyContext;
import com.github.lhervier.domino.oauth.server.testsuite.impl.NotesPrincipalTestImpl;

@SuppressWarnings("serial")
public class TestAuthCodeGrant extends BaseTest {

	/**
	 * "normal" application.
	 */
	private static final String APP_NAME = "myApp";
	private static final String APP_FULL_NAME = "CN=myApp/OU=APPLICATION/O=WEB";
	private static final String APP_CLIENT_ID = "1234";
	private static final String APP_REDIRECT_URI = "http://acme.com/myApp";
	private ApplicationEntity normalApp;
	
	/**
	 * Hacky app: Try to declare another app that points to the same redirect uri
	 */
	private static final String HACKY_APP_NAME = "hackyApp";
	private static final String HACKY_APP_FULL_NAME = "CN=hackyApp/OU=APPLICATION/O=WEB";
	private static final String HACKY_APP_CLIENT_ID = "3456";
	private static final String HACKY_APP_REDIRECT_URI = "http://acme.com/myApp";
	private ApplicationEntity hackyApp;
	
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
	 * Auth code service
	 */
	@Autowired
	private AuthCodeService authCodeSvcMock;
	
	/**
	 * Extension service mock
	 */
	@Autowired
	private ExtensionService extSvcMock;
	
	/**
	 * User principal
	 */
	@Autowired
	protected NotesPrincipalTestImpl user;
	
	public static class TokenResponse {
		@JsonProperty("refresh_token")
		private String refreshToken;
		@JsonProperty("expires_in")
		private long expiresIn;
		private String scope;
		@JsonProperty("dummy_user")
		private String dummyUser;
		@JsonProperty("dummy_token_param")
		private String dummyTokenParam;
		public String getRefreshToken() { return refreshToken; }
		public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
		public long getExpiresIn() { return expiresIn; }
		public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
		public String getScope() { return scope; }
		public void setScope(String scope) { this.scope = scope; }
		public String getDummyUser() { return dummyUser; }
		public void setDummyUser(String dummyUser) { this.dummyUser = dummyUser; }
		public String getDummyTokenParam() { return dummyTokenParam; }
		public void setDummyTokenParam(String dummyTokenParam) { this.dummyTokenParam = dummyTokenParam; }
	};
	
	/**
	 * Before
	 */
	@Before
	public void before() throws IOException {
		// Reset repositories
		reset(appRepoMock);
		reset(authCodeRepoMock);
		reset(authCodeSvcMock);
		reset(extSvcMock);
		
		// Declare applications
		this.normalApp = new ApplicationEntity() {{
			this.setClientId(APP_CLIENT_ID);
			this.setFullName(APP_FULL_NAME);
			this.setName(APP_NAME);
			this.setRedirectUri(APP_REDIRECT_URI);
		}};
		when(this.appRepoMock.findOne(eq(APP_CLIENT_ID))).thenReturn(normalApp);
		when(this.appRepoMock.findOneByName(eq(APP_NAME))).thenReturn(normalApp);
		
		this.hackyApp = new ApplicationEntity() {{
			this.setClientId(HACKY_APP_CLIENT_ID);
			this.setFullName(HACKY_APP_FULL_NAME);
			this.setName(HACKY_APP_NAME);
			this.setRedirectUri(HACKY_APP_REDIRECT_URI);
		}};
		when(this.appRepoMock.findOne(eq(HACKY_APP_CLIENT_ID))).thenReturn(hackyApp);
		when(this.appRepoMock.findOneByName(eq(HACKY_APP_NAME))).thenReturn(hackyApp);
		
		// Login as normal application.
		user.setAuthType(AuthType.NOTES);
		user.setName(APP_FULL_NAME);
		user.setCommon(APP_NAME);
		user.setRoles(new ArrayList<String>());
		user.setClientId(null);
		user.setScopes(null);
		user.setCurrentDatabasePath(this.oauth2Db);
	}
	
	// ======================================================================
	
	/**
	 * Get tokens from auth code using GET is forbidden
	 */
	@Test
	public void whenNotUsingPost_then500() throws Exception {
		this.mockMvc.perform(get("/token"))
		.andExpect(status().is(500))		// RFC is not specifying that we must return a 400 error here. 500 is spring default behaviour and we don't want to change it...
		.andExpect(content().string(containsString("Request method 'GET' not supported")));
	}
	
	/**
	 * Code is mandatory
	 */
	@Test
	public void whenEmptyCode_then400() throws Exception {
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
	public void whenWrongRedirectUri_then400() throws Exception {
		when(this.authCodeRepoMock.findOne(eq("AZERTY"))).thenReturn(new AuthCodeEntity() {{
			setId("AZERTY");
			setApplication(APP_NAME);
			setClientId(APP_CLIENT_ID);
			setExpires(timeSvcStub.currentTimeSeconds() + 10L);
			setRedirectUri("http://acme.com/myApp");
		}});
		
		this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("code", "AZERTY")
				.param("redirect_uri", "http://wrong.redirect/uri")
		).andExpect(status().is(400))
		.andExpect(content().string(containsString("invalid redirect_uri")));
	}
	
	/**
	 * No redirect uri in URI, and additional uris in app
	 */
	@Test
	public void whenNoRedirectUriEvenInApp_then400() throws Exception {
		this.normalApp.setRedirectUris(Arrays.asList("http://acme.com/aditionnal"));
		this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("code", "AZERTY")
		).andExpect(status().is(400))
		.andExpect(content().string(containsString("redirect_uri is mandatory")));
	}
	
	/**
	 * Invalid auth code
	 */
	@Test
	public void whenInvalidAuthCode_then400() throws Exception {
		this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("code", "invalid_auth_code")
				.param("redirect_uri", APP_REDIRECT_URI)
		).andExpect(status().is(400))
		.andExpect(content().string(containsString("invalid auth code")));
	}
	
	/**
	 * Expires auth code
	 */
	@Test
	public void whenExpiredAuthCode_then400() throws Exception {
		when(this.authCodeRepoMock.findOne(eq("AZERTY"))).thenReturn(new AuthCodeEntity() {{
			setId("AZERTY");
			setApplication(APP_NAME);
			setClientId(APP_CLIENT_ID);
			setExpires(timeSvcStub.currentTimeSeconds() - 10L);
			setRedirectUri("http://acme.com/myApp");
		}});
		
		this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("code", "AZERTY")
				.param("redirect_uri", APP_REDIRECT_URI)
		).andExpect(status().is(400))
		.andExpect(content().string(containsString("code has expired")));
	}
	
	/**
	 * Auth code must have been generated for the
	 * currently authenticated app
	 */
	@Test
	public void whenWrongClientId_then400() throws Exception {
		// Login as hacky app
		user.setName(HACKY_APP_FULL_NAME);
		user.setCommon(HACKY_APP_NAME);
		
		when(this.authCodeRepoMock.findOne(eq("AZERTY"))).thenReturn(new AuthCodeEntity() {{
			setId("AZERTY");
			setApplication(APP_NAME);
			setClientId(APP_CLIENT_ID);
			setExpires(timeSvcStub.currentTimeSeconds() + 10L);
			setRedirectUri("http://acme.com/myApp");
		}});
		
		// Try to exchange the auth code from normal app
		this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("code", "AZERTY")
				.param("redirect_uri", APP_REDIRECT_URI)
		).andExpect(status().is(400))
		.andExpect(content().string(containsString("code generated for another app")));
	}
	
	/**
	 * Scopes must be present in response if granted scopes are different from asked scope (in authorize request)
	 */
	@Test
	public void whenScopesGrantedDifferentFromScopesAsked_thenScopePresentInResponse() throws Exception {
		// Same scopes
		when(this.authCodeRepoMock.findOne(eq("AZERTY"))).thenReturn(new AuthCodeEntity() {{
			setId("AZERTY");
			setApplication(APP_NAME);
			setClientId(APP_CLIENT_ID);
			setExpires(timeSvcStub.currentTimeSeconds() + 10L);
			setRedirectUri("http://acme.com/myApp");
			setGrantedScopes(Arrays.asList("scope1", "scope2", "scope3"));
			setScopes(Arrays.asList("scope1", "scope2", "scope3"));
		}});
		
		// Granted scopes = Asked scopes => No scope attribute in response
		MvcResult result = this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("code", "AZERTY")
				.param("redirect_uri", APP_REDIRECT_URI)
		).andExpect(status().is(200))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		TokenResponse response = this.mapper.readValue(json, TokenResponse.class);
		assertThat(response.getScope(), nullValue());
		
		// Remove one scope from the granted set
		when(this.authCodeRepoMock.findOne(eq("AZERTY"))).thenReturn(new AuthCodeEntity() {{
			setId("AZERTY");
			setApplication(APP_NAME);
			setClientId(APP_CLIENT_ID);
			setExpires(timeSvcStub.currentTimeSeconds() + 10L);
			setRedirectUri("http://acme.com/myApp");
			setGrantedScopes(Arrays.asList("scope1", "scope3"));
			setScopes(Arrays.asList("scope1", "scope2", "scope3"));
		}});
		
		// Granted scopes < Asked Scopes => Scope attribute in response
		result = this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("code", "AZERTY")
				.param("redirect_uri", APP_REDIRECT_URI)
		).andExpect(status().is(200))
		.andReturn();
		
		json = result.getResponse().getContentAsString();
		response = this.mapper.readValue(json, TokenResponse.class);
		assertThat(response.getScope(), equalTo("scope1 scope3"));
	}
	
	/**
	 * Get tokens from auth code. Client is public.
	 */
	@Test
	public void whenPublicClient_thenNoRefreshToken() throws Exception {
		when(this.authCodeRepoMock.findOne(eq("AZERTY"))).thenReturn(new AuthCodeEntity() {{
			setId("AZERTY");
			setApplication(APP_NAME);
			setClientId(APP_CLIENT_ID);
			setExpires(timeSvcStub.currentTimeSeconds() + 10L);
			setRedirectUri("http://acme.com/myApp");
		}});
		
		when(this.authCodeSvcMock.fromEntity(Mockito.any(AuthCodeEntity.class)))
		.thenReturn("012345");
		
		MvcResult result = this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("redirect_uri", APP_REDIRECT_URI)
				.param("code", "AZERTY")
		).andExpect(status().is(200))
		.andExpect(content().contentType("application/json;charset=UTF-8"))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		
		TokenResponse resp = this.mapper.readValue(json, TokenResponse.class);
		assertThat(resp.getRefreshToken(), is(nullValue()));			// No refresh token
		assertThat(resp.getScope(), nullValue());
		assertThat(resp.getExpiresIn(), is(equalTo(36000L)));
	}
	
	/**
	 * Get tokens from auth code. Client type is not defined.
	 */
	@Test
	public void whenUndefinedClient_thenNoRefreshToken() throws Exception {
		normalApp.setClientType(null);
		
		when(this.authCodeRepoMock.findOne(eq("AZERTY"))).thenReturn(new AuthCodeEntity() {{
			setId("AZERTY");
			setApplication(APP_NAME);
			setClientId(APP_CLIENT_ID);
			setExpires(timeSvcStub.currentTimeSeconds() + 10L);
			setRedirectUri("http://acme.com/myApp");
		}});
		
		when(this.authCodeSvcMock.fromEntity(Mockito.any(AuthCodeEntity.class)))
		.thenReturn("012345");
		
		MvcResult result = this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("redirect_uri", APP_REDIRECT_URI)
				.param("code", "AZERTY")
		).andExpect(status().is(200))
		.andExpect(content().contentType("application/json;charset=UTF-8"))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		
		TokenResponse resp = this.mapper.readValue(json, TokenResponse.class);
		assertThat(resp.getRefreshToken(), is(nullValue()));			// No refresh token
		assertThat(resp.getScope(), nullValue());
		assertThat(resp.getExpiresIn(), is(equalTo(36000L)));
	}
	
	/**
	 * Get tokens from auth code. Client is confidential.
	 */
	@Test
	public void whenConfidentialClient_thenRefreshToken() throws Exception {
		normalApp.setClientType(AppServiceImpl.CLIENTTYPE_CONFIDENTIAL);
		
		when(this.authCodeRepoMock.findOne(eq("AZERTY"))).thenReturn(new AuthCodeEntity() {{
			setId("AZERTY");
			setApplication(APP_NAME);
			setClientId(APP_CLIENT_ID);
			setExpires(timeSvcStub.currentTimeSeconds() + 10L);
			setRedirectUri("http://acme.com/myApp");
		}});
		
		when(this.authCodeSvcMock.fromEntity(Mockito.any(AuthCodeEntity.class)))
		.thenReturn("012345");
		
		MvcResult result = this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("redirect_uri", APP_REDIRECT_URI)
				.param("code", "AZERTY")
		).andExpect(status().is(200))
		.andExpect(content().contentType("application/json;charset=UTF-8"))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		
		TokenResponse resp = this.mapper.readValue(json, TokenResponse.class);
		assertThat(resp.getRefreshToken(), is(notNullValue()));			// Refresh token !
		assertThat(resp.getScope(), nullValue());
		assertThat(resp.getExpiresIn(), is(equalTo(36000L)));
	}
	
	/**
	 * Extensions in conflict
	 */
	@Test
	public void whenExtensionsInConflict_then400() throws Exception {
		when(this.authCodeRepoMock.findOne(eq("AZERTY"))).thenReturn(new AuthCodeEntity() {{
			setId("AZERTY");
			setApplication(APP_NAME);
			setClientId(APP_CLIENT_ID);
			setExpires(timeSvcStub.currentTimeSeconds() + 10L);
			setRedirectUri("http://acme.com/myApp");
			setContextClasses(new HashMap<String, String>() {{
				put("dummy1", DummyContext.class.getName());
				put("dummy2", DummyContext.class.getName());
				// No context for dummy3
			}});
			setContextObjects(new HashMap<String, String>() {{
				put("dummy1", mapper.writeValueAsString(new DummyContext() {{
					setName("CN=Lionel/O=USER");
				}}));
				put("dummy2", mapper.writeValueAsString(new DummyContext() {{
					setName("CN=François/O=USER");
				}}));
				// No context for dummy3
			}});
		}});
		
		when(extSvcMock.getResponseTypes()).thenReturn(Arrays.asList("dummy1", "dummy2", "dummy3"));
		when(extSvcMock.getExtension(eq("dummy1"))).thenReturn(new IOAuthAuthorizeExtension() {
			public List<String> getAuthorizedScopes() { return Arrays.asList(); }
			public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, IAuthorizer authorizer) { }
			public void token(NotesPrincipal user, Application app, Object context, List<String> askedScopes, IPropertyAdder adder) {
				adder.addProperty("prop", "value");
			}
		});
		when(extSvcMock.getExtension(eq("dummy2"))).thenReturn(new IOAuthAuthorizeExtension() {
			public List<String> getAuthorizedScopes() { return Arrays.asList(); }
			public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, IAuthorizer authorizer) { }
			public void token(NotesPrincipal user, Application app, Object context, List<String> askedScopes, IPropertyAdder adder) {
				adder.addProperty("prop", "value");
			}
		});
		when(extSvcMock.getExtension(eq("dummy3"))).thenReturn(new IOAuthAuthorizeExtension() {
			public List<String> getAuthorizedScopes() { return Arrays.asList(); }
			public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, IAuthorizer authorizer) { }
			public void token(NotesPrincipal user, Application app, Object context, List<String> askedScopes, IPropertyAdder adder) {
				adder.addProperty("prop", "value");
			}
		});
		
		MvcResult result = this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("redirect_uri", APP_REDIRECT_URI)
				.param("code", "AZERTY")
		).andExpect(status().is(400))
		.andExpect(content().contentType("application/json;charset=UTF-8"))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		@SuppressWarnings("unchecked")
		Map<String, String> response = this.mapper.readValue(json, Map.class);
		assertThat(response, hasEntry("error", "server_error"));
	}
	

	/**
	 * AuthCode is removed after grant
	 */
	@Test
	public void whenGrant_thenAuthCodeRemoved() throws Exception {
		when(this.authCodeRepoMock.findOne(eq("12345"))).thenReturn(new AuthCodeEntity() {{
			setExpires(timeSvcStub.currentTimeSeconds() + 10L);
			setClientId(APP_CLIENT_ID);
			setApplication(APP_NAME);
			setRedirectUri(APP_REDIRECT_URI);
		}});
		this.mockMvc
		.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("code", "12345")
				.param("redirect_uri", APP_REDIRECT_URI)
		).andExpect(status().is(200));
		
		verify(this.authCodeRepoMock, times(1)).delete(eq("12345"));
	}
}
