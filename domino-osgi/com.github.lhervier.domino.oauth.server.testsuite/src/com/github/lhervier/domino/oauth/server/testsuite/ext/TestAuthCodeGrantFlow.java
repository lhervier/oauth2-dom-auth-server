package com.github.lhervier.domino.oauth.server.testsuite.ext;

import static com.github.lhervier.domino.oauth.server.testsuite.utils.TestUtils.urlParameters;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import com.github.lhervier.domino.oauth.server.NotesPrincipal.AuthType;
import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ext.core.AccessToken;
import com.github.lhervier.domino.oauth.server.model.ClientType;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.impl.NotesPrincipalTestImpl;
import com.github.lhervier.domino.oauth.server.testsuite.impl.TimeServiceTestImpl;

public class TestAuthCodeGrantFlow extends BaseTest {

	@Autowired
	protected NotesPrincipalTestImpl user;
	
	private final Map<String, AuthCodeEntity> authCodes = new HashMap<String, AuthCodeEntity>();
	
	@Before
	public void before() {
		when(extSvcMock.getResponseTypes()).thenCallRealMethod();
		when(extSvcMock.getExtension(anyString())).thenCallRealMethod();
		
		ApplicationEntity app = new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp/O=APP");
			setRedirectUri("http://acme.com/myApp");
			setClientType(ClientType.CONFIDENTIAL.name());
		}};
		when(this.appRepoMock.findOne(eq("1234"))).thenReturn(app);
		when(this.appRepoMock.findOneByName(eq("myApp"))).thenReturn(app);
		
		authCodes.clear();
		when(this.authCodeRepoMock.findOne(anyString())).thenAnswer(new Answer<AuthCodeEntity>() {
			@Override
			public AuthCodeEntity answer(InvocationOnMock invocation) throws Throwable {
				return authCodes.get(invocation.getArguments()[0].toString());
			}
		});
		when(this.authCodeRepoMock.save(any(AuthCodeEntity.class))).thenAnswer(new Answer<AuthCodeEntity>() {
			@Override
			public AuthCodeEntity answer(InvocationOnMock invocation) throws Throwable {
				AuthCodeEntity code = invocation.getArgumentAt(0, AuthCodeEntity.class);
				authCodes.put(code.getId(), code);
				return code;
			}
		});
		when(this.authCodeRepoMock.delete(anyString())).thenAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				String code = invocation.getArgumentAt(0, String.class);
				authCodes.remove(code);
				return true;
			}
		});
		
	}
	
	@Test
	public void whenAskForToken_thenReturnTokens() throws Exception {
		// Log in as user
		user.setAuthType(AuthType.NOTES);
		user.setName("CN=Lionel/O=USER");
		user.setCommon("Lionel");
		user.setClientId(null);
		user.setScopes(null);
		user.setCurrentDatabasePath(this.oauth2Db);
		
		// Ask for authorization code
		MvcResult result = this.mockMvc.perform(
				get("/authorize")
				.param("response_type", "code")
				.param("client_id", "1234")
				.param("redirect_uri", "http://acme.com/myApp")
				.param("scope", "scope1 scope2")
		).andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		assertThat(location, startsWith("http://acme.com/myApp?"));
		
		Map<String, String> params = urlParameters(location);
		assertThat(params.size(), equalTo(1));
		assertThat(params, hasKey("code"));
		String code = params.get("code");
		
		// Login as application
		user.setAuthType(AuthType.NOTES);
		user.setName("CN=myApp/O=APP");
		user.setCommon("myApp");
		user.setClientId(null);
		user.setScopes(null);
		user.setCurrentDatabasePath(this.oauth2Db);
		
		// Exchange authorization code for an access_token and a refresh_token
		result = this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("code", code)
		).andExpect(status().is(200))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		Map<String, Object> tkResp = this.fromJson(json);
		
		assertThat(tkResp.size(), equalTo(5));
		assertThat(tkResp, hasKey("access_token"));
		assertThat(tkResp, hasKey("token_type"));
		assertThat(tkResp, hasKey("refresh_token"));
		assertThat(tkResp, hasKey("scope"));
		assertThat(tkResp, hasKey("expires_in"));
		
		assertThat(tkResp.get("token_type").toString(), equalTo("bearer"));
		assertThat(tkResp.get("scope").toString(), equalTo(""));
		assertThat((Integer) tkResp.get("expires_in"), equalTo((int) coreExpiresIn));
		
		String accessToken = tkResp.get("access_token").toString();
		String refreshToken = tkResp.get("refresh_token").toString();
		
		AccessToken accTk = this.jwtSvc.fromJws(accessToken, coreSignKey, AccessToken.class);
		assertThat(accTk, notNullValue());
		AuthCodeEntity refTk = this.jwtSvc.fromJwe(refreshToken, refreshTokenConfig, AuthCodeEntity.class);
		assertThat(refTk, notNullValue());
		
		assertThat(accTk.getSub(), equalTo("CN=Lionel/O=USER"));
		assertThat(accTk.getAud(), equalTo("1234"));
		assertThat(accTk.getIss(), equalTo(coreIss));
		assertThat(accTk.getExpires(), equalTo(this.timeSvc.currentTimeSeconds() + coreExpiresIn));
		
		assertThat(refTk.getScopes(), containsInAnyOrder("scope1", "scope2"));
		assertThat(refTk.getGrantedScopes().size(), equalTo(0));
		assertThat(refTk.getExpires(), equalTo(this.timeSvc.currentTimeSeconds() + refreshTokenLifetime));
		
		// Ask for token details (still logged in as app)
		// FIXME: Log in as another app
		result = this.mockMvc.perform(
				post("/checkToken")
				.param("token", accessToken)
		).andExpect(status().is(200))
		.andReturn();
		
		json = result.getResponse().getContentAsString();
		Map<String, Object> checkTkResp = this.fromJson(json);
		assertThat(checkTkResp, hasEntry("active", (Object) true));
		assertThat(checkTkResp, hasEntry("sub", (Object) "CN=Lionel/O=USER"));
		assertThat(checkTkResp, hasEntry("scope", (Object) ""));
		assertThat(checkTkResp, hasEntry("client_id", (Object) "1234"));
		assertThat(checkTkResp, hasEntry("username", (Object) "CN=Lionel/O=USER"));
		assertThat(checkTkResp, hasEntry("user_name", (Object) "CN=Lionel/O=USER"));		// Needed by Spring Security
		assertThat(checkTkResp, hasEntry("token_type", (Object) "bearer"));
		assertThat(checkTkResp, hasEntry("exp", (Object) new Integer((int) (timeSvc.currentTimeSeconds() + coreExpiresIn))));
		assertThat(checkTkResp, hasEntry("sub", (Object) "CN=Lionel/O=USER"));
		assertThat(checkTkResp, hasEntry("iss", (Object) coreIss));
		assertThat(checkTkResp, not(hasKey("aud")));		// Will make Spring Security fail otherwise
		
		// Wait for the access token to expire
		TimeServiceTestImpl.CURRENT_TIME += coreExpiresIn + 10L;
		
		// Ask for details again
		result = this.mockMvc.perform(
				post("/checkToken")
				.param("token", accessToken)
		).andExpect(status().is(200))
		.andReturn();
		
		json = result.getResponse().getContentAsString();
		checkTkResp = this.fromJson(json);
		assertThat(checkTkResp, hasEntry("active", (Object) false));
		
		// Use the refresh_token to get a new valid access token
		result = this.mockMvc.perform(
				post("/token")
				.param("grant_type", "refresh_token")
				.param("refresh_token", refreshToken)
		).andExpect(status().is(200))
		.andReturn();
		
		json = result.getResponse().getContentAsString();
		Map<String, Object> refreshTkResp = this.fromJson(json);
		
		assertThat(refreshTkResp.size(), equalTo(4));
		assertThat(refreshTkResp, hasKey("access_token"));
		assertThat(refreshTkResp, hasKey("token_type"));
		assertThat(refreshTkResp, hasKey("refresh_token"));
		assertThat(refreshTkResp, hasKey("expires_in"));
		
		String newAccessToken = refreshTkResp.get("access_token").toString();
		String newRefreshToken = refreshTkResp.get("refresh_token").toString();
		
		AccessToken newAccTk = this.jwtSvc.fromJws(newAccessToken, coreSignKey, AccessToken.class);
		assertThat(newAccTk, notNullValue());
		AuthCodeEntity newRefTk = this.jwtSvc.fromJwe(newRefreshToken, refreshTokenConfig, AuthCodeEntity.class);
		assertThat(newRefTk, notNullValue());
		
		assertThat(newAccTk.getSub(), equalTo("CN=Lionel/O=USER"));
		assertThat(newAccTk.getAud(), equalTo("1234"));
		assertThat(newAccTk.getIss(), equalTo(coreIss));
		assertThat(newAccTk.getExpires(), equalTo(this.timeSvc.currentTimeSeconds() + coreExpiresIn));
		
		assertThat(newRefTk.getScopes(), containsInAnyOrder("scope1", "scope2"));
		assertThat(newRefTk.getGrantedScopes().size(), equalTo(0));
		assertThat(newRefTk.getExpires(), equalTo(this.timeSvc.currentTimeSeconds() + refreshTokenLifetime));
		
	}
}
