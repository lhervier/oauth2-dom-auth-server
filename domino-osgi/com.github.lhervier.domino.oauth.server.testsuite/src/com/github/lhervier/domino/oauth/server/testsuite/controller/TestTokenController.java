package com.github.lhervier.domino.oauth.server.testsuite.controller;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.codehaus.jackson.annotate.JsonProperty;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
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

	@Autowired
	private ApplicationRepository appRepoMock;
	
	@Autowired
	private AuthCodeRepository authCodeRepoMock;
	
	@Autowired
	private CoreExt coreExt;
	
	@Autowired
	protected NotesPrincipalTestImpl user;
	
	@Before
	public void setUp() {
		reset(appRepoMock);
		reset(authCodeRepoMock);
		
		user.setAuthType(AuthType.NOTES);
		user.setName("CN=myApp/OU=APPLICATION/O=WEB");
		user.setCommon("myApp");
		user.setRoles(new ArrayList<String>());
		user.setClientId(null);
		user.setScopes(null);
		user.setCurrentDatabasePath(this.oauth2Db);
	}
	
	/**
	 * Mock the app repo
	 */
	private void mockApp() throws Exception {
		ApplicationEntity app = new ApplicationEntity() {{
			this.setAppReader("CN=myApp/OU=APPLICATION/O=WEB");
			this.setClientId("1234");
			this.setFullName("CN=myApp/OU=APPLICATION/O=WEB");
			this.setName("myApp");
			this.setReaders("*");
			this.setRedirectUri("http://acme.com/myApp");
			this.setRedirectUris(new ArrayList<String>());
		}};
		when(this.appRepoMock.findOne(eq("1234"))).thenReturn(app);
		when(this.appRepoMock.findOneByName(eq("myApp"))).thenReturn(app);
	}
	
	/**
	 * Mock the auth code repo
	 */
	private void mockAuthCode() throws Exception {
		when(this.authCodeRepoMock.findOne(eq("012345"))).thenReturn(new AuthCodeEntity() {{
			this.setId("012345");
			this.setApplication("myApp");
			this.setClientId("1234");
			this.setExpires(TimeServiceTestImpl.CURRENT_TIME + 600);
			this.setScopes(Arrays.asList("scope1", "scope2"));
			this.setGrantedScopes(new ArrayList<String>());
			this.setRedirectUri("http://acme.com/myApp");
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
							this.setAud("1234");
							this.setSub("CN=Lionel/o=USER");
							this.setIss("https://acme.com/domino/oauth2/");
						}})
				);
			}});
		}});
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
	
	
	/**
	 * Get tokens from auth code using GET is forbidden
	 */
	@Test
	public void tokensFromAuthCodeWithGet() throws Exception {
		this.mockAuthCode();
		this.mockApp();
		
		this.mockMvc.perform(
				get("/token")
				.param("grant_type", "authorization_code")
				.param("code", "012345")
		).andExpect(status().is(500))
		.andExpect(content().string(CoreMatchers.containsString("Request method 'GET' not supported")));
	}
	
	/**
	 * Get tokens from auth code
	 */
	@Test
	public void tokensFromAuthCode() throws Exception {
		this.mockAuthCode();
		this.mockApp();
		
		MvcResult result = this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("code", "012345")
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
