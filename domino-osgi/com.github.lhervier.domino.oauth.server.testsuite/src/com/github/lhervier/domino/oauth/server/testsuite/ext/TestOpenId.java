package com.github.lhervier.domino.oauth.server.testsuite.ext;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
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

import java.util.Arrays;
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
import com.github.lhervier.domino.oauth.server.entity.PersonEntity;
import com.github.lhervier.domino.oauth.server.ext.openid.IdToken;
import com.github.lhervier.domino.oauth.server.model.ClientType;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.impl.NotesPrincipalTestImpl;
import com.github.lhervier.domino.oauth.server.testsuite.impl.TimeServiceTestImpl;
import com.github.lhervier.domino.oauth.server.testsuite.utils.TestUtils;

public class TestOpenId extends BaseTest {

	@Autowired
	protected NotesPrincipalTestImpl user;
	
	@Before
	public void before() {
		when(extSvcMock.getResponseTypes()).thenCallRealMethod();
		when(extSvcMock.getExtension(anyString())).thenCallRealMethod();
		
		// Mocking app repo
		ApplicationEntity app = new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp/O=APP");
			setRedirectUri("http://acme.com/myApp");
			setClientType(ClientType.CONFIDENTIAL.name());
		}};
		when(this.appRepoMock.findOne(eq("1234"))).thenReturn(app);
		when(this.appRepoMock.findOneByName(eq("myApp"))).thenReturn(app);
		
		// Mocking person repo
		when(this.personRepoMock.findOne("CN=Lionel HERVIER/O=USER")).thenReturn(new PersonEntity() {{
			setFirstName("Lionel");
			setLastName("HERVIER");
			setFullNames(Arrays.asList("CN=Lionel HERVIER/O=USER"));
			setInternetAddress("lhervier@asi.fr");
			setMiddleInitial("jr");
			setOfficePhoneNumber("+33412345678");
			setPhotoUrl("http://acme.com/photos/lionel");
			setShortName("lio");
			setTitle("Mr");
			setWebsite("https://github.com/lhervier");
		}});
		
		// Mocking auth code repo
		final Map<String, AuthCodeEntity> authCodes = new HashMap<String, AuthCodeEntity>();
		when(this.authCodeRepoMock.save(any(AuthCodeEntity.class))).then(new Answer<AuthCodeEntity>() {
			public AuthCodeEntity answer(InvocationOnMock invocation) throws Throwable {
				AuthCodeEntity entity = invocation.getArgumentAt(0, AuthCodeEntity.class);
				authCodes.put(entity.getId(), entity);
				return entity;
			}
		});
		when(this.authCodeRepoMock.delete(anyString())).then(new Answer<Boolean>() {
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				authCodes.remove(invocation.getArgumentAt(0, String.class));
				return true;
			}
		});
		when(this.authCodeRepoMock.findOne(anyString())).then(new Answer<AuthCodeEntity>() {
			public AuthCodeEntity answer(InvocationOnMock invocation) throws Throwable {
				return authCodes.get(invocation.getArgumentAt(0, String.class));
			}
		});
		
		
		user.setAuthType(AuthType.NOTES);
		user.setName("CN=Lionel HERVIER/O=USER");
		user.setCommon("Lionel HERVIER");
		user.setClientId(null);
		user.setScopes(null);
		user.setCurrentDatabasePath(this.oauth2Db);
	}
	
	/**
	 * response_type=id_token token
	 */
	@Test
	public void whenUsingIdTokenTokenResponseType_thenReturnIdToken() throws Exception {
		MvcResult result = this.mockMvc.perform(
				get("/authorize")
				.param("response_type", "token id_token")
				.param("client_id", "1234")
				.param("scope", "openid profile email scope1")
		).andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		assertThat(location, startsWith("http://acme.com/myApp#"));
		
		Map<String, String> frag = TestUtils.urlRefs(location);
		assertThat(frag, hasKey("access_token"));
		assertThat(frag, hasKey("expires_in"));
		assertThat(frag, hasKey("id_token"));
		assertThat(frag, not(hasKey("refresh_token")));
		assertThat(frag, not(hasKey("code")));
		
		String sIdTk = frag.get("id_token");
		IdToken idTk = this.jwtSvc.fromJws(sIdTk, this.openidSignKey, IdToken.class);
		
		// Expiration and issued at
		assertThat(idTk.getExpires(), equalTo(timeSvc.currentTimeSeconds() + this.openidExpiresIn));
		assertThat(idTk.getAuthTime(), equalTo(timeSvc.currentTimeSeconds()));
		assertThat(idTk.getIat(), equalTo(timeSvc.currentTimeSeconds()));
		
		// Std JWT properties
		assertThat(idTk.getAud(), equalTo("1234"));
		assertThat(idTk.getIss(), equalTo(this.openidIss));
		assertThat(idTk.getSub(), equalTo("CN=Lionel HERVIER/O=USER"));
		
		// properties injected because of "profile" scope
		assertThat(idTk.getName(), equalTo("CN=Lionel HERVIER/O=USER"));
		assertThat(idTk.getFamilyName(), equalTo("HERVIER"));
		assertThat(idTk.getMiddleName(), equalTo("jr"));
		assertThat(idTk.getGender(), equalTo("Mr"));
		assertThat(idTk.getPreferedUsername(), equalTo("lio"));
		assertThat(idTk.getWebsite(), equalTo("https://github.com/lhervier"));
		assertThat(idTk.getPicture(), equalTo("http://acme.com/photos/lionel"));
		
		// properties injected because of "email" scope
		assertThat(idTk.getEmail(), equalTo("lhervier@asi.fr"));
		
		// Other properties NOT injected (because scopes not specified)
		assertThat(idTk.getAddress(), nullValue());
		assertThat(idTk.getPhoneNumber(), nullValue());
	}
	
	/**
	 * When using nonce, then nonce propagated in id token
	 */
	@Test
	public void whenNonceInIdTokenTokenRequest_thenNonceInIdToken() throws Exception {
		MvcResult result = this.mockMvc.perform(
				get("/authorize")
				.param("response_type", "token id_token")
				.param("client_id", "1234")
				.param("scope", "openid profile email scope1")
				.param("nonce", "azerty")		// Nonce in request
		).andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		assertThat(location, startsWith("http://acme.com/myApp#"));
		
		Map<String, String> frag = TestUtils.urlRefs(location);
		assertThat(frag, hasKey("id_token"));
		
		String sIdTk = frag.get("id_token");
		IdToken idTk = this.jwtSvc.fromJws(sIdTk, this.openidSignKey, IdToken.class);
		
		assertThat(idTk.getNonce(), equalTo("azerty"));
	}
	
	/**
	 * response_type=code id_token
	 */
	@Test
	public void whenUsingCodeIdTokenResponseType_thenReturnIdTokenInGrant() throws Exception {
		// Get authorization code and id_token
		MvcResult result = this.mockMvc.perform(
				get("/authorize")
				.param("response_type", "code id_token")
				.param("client_id", "1234")
				.param("scope", "openid profile email scope1")
		).andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		assertThat(location, startsWith("http://acme.com/myApp?"));
		
		Map<String, String> frag = TestUtils.urlParameters(location);
		assertThat(frag, hasKey("code"));
		assertThat(frag, not(hasKey("id_token")));
		assertThat(frag, not(hasKey("access_token")));
		assertThat(frag, not(hasKey("expires_in")));
		assertThat(frag, not(hasKey("refresh_token")));
		
		String code = frag.get("code");
		
		// Get id_token from authorization code
		TimeServiceTestImpl.CURRENT_TIME += 2L;		// 2s pass
		user.setName("CN=myApp/O=APP");
		user.setCommon("myApp");
		
		result = this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("code", code)
		).andExpect(status().is(200))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		Map<String, Object> tkResp = this.fromJson(json);
		
		assertThat(tkResp, hasKey("access_token"));
		assertThat(tkResp, hasKey("refresh_token"));
		assertThat(tkResp, hasKey("expires_in"));
		assertThat(tkResp, hasKey("id_token"));
		
		String sIdTk = (String) tkResp.get("id_token");
		IdToken idTk = this.jwtSvc.fromJws(sIdTk, this.openidSignKey, IdToken.class);
		
		// Expiration and issued at
		assertThat(idTk.getExpires(), equalTo(timeSvc.currentTimeSeconds() + this.openidExpiresIn));
		assertThat(idTk.getAuthTime(), equalTo(timeSvc.currentTimeSeconds() - 2L));		// Authenticated 2s ago
		assertThat(idTk.getIat(), equalTo(timeSvc.currentTimeSeconds()));
		
		// Std JWT properties
		assertThat(idTk.getAud(), equalTo("1234"));
		assertThat(idTk.getIss(), equalTo(this.openidIss));
		assertThat(idTk.getSub(), equalTo("CN=Lionel HERVIER/O=USER"));
		
		// properties injected because of "profile" scope
		assertThat(idTk.getName(), equalTo("CN=Lionel HERVIER/O=USER"));
		assertThat(idTk.getFamilyName(), equalTo("HERVIER"));
		assertThat(idTk.getMiddleName(), equalTo("jr"));
		assertThat(idTk.getGender(), equalTo("Mr"));
		assertThat(idTk.getPreferedUsername(), equalTo("lio"));
		assertThat(idTk.getWebsite(), equalTo("https://github.com/lhervier"));
		assertThat(idTk.getPicture(), equalTo("http://acme.com/photos/lionel"));
		
		// properties injected because of "email" scope
		assertThat(idTk.getEmail(), equalTo("lhervier@asi.fr"));
		
		// Other properties NOT injected (because scopes not specified)
		assertThat(idTk.getAddress(), nullValue());
		assertThat(idTk.getPhoneNumber(), nullValue());
	}
	
	/**
	 * When nonce in authorize request, then nonce is grant response
	 */
	@Test
	public void whenNonceInRequest_thenNonceInGrantResponse() throws Exception {
		// Get authorization code and id_token
		MvcResult result = this.mockMvc.perform(
				get("/authorize")
				.param("response_type", "code id_token")
				.param("client_id", "1234")
				.param("scope", "openid")
				.param("nonce", "azerty")
		).andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		Map<String, String> frag = TestUtils.urlParameters(location);
		String code = frag.get("code");
		
		// Get id_token from authorization code
		user.setName("CN=myApp/O=APP");
		user.setCommon("myApp");
		result = this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("code", code)
		).andExpect(status().is(200))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		Map<String, Object> tkResp = this.fromJson(json);
		
		String sIdTk = (String) tkResp.get("id_token");
		IdToken idTk = this.jwtSvc.fromJws(sIdTk, this.openidSignKey, IdToken.class);
		
		assertThat(idTk.getNonce(), equalTo("azerty"));
	}
	
	/**
	 * When not openid scope, then no id token
	 */
	@Test
	public void whenNoOpenIdScopeInTokenRequest_thenNoIdToken() throws Exception {
		MvcResult result = this.mockMvc.perform(
				get("/authorize")
				.param("response_type", "token id_token")
				.param("client_id", "1234")
				.param("scope", "profile")
		).andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		assertThat(location, startsWith("http://acme.com/myApp#"));
		
		Map<String, String> frag = TestUtils.urlRefs(location);
		assertThat(frag, not(hasKey("id_token")));
		assertThat(frag, hasEntry("scope", ""));
	}
	
	/**
	 * When not openid scope, then no id token
	 */
	@Test
	public void whenNoOpenIdScopeInCodeRequest_thenNoIdToken() throws Exception {
		MvcResult result = this.mockMvc.perform(
				get("/authorize")
				.param("response_type", "code id_token")
				.param("client_id", "1234")
				.param("scope", "profile")
		).andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		assertThat(location, startsWith("http://acme.com/myApp?"));
		
		Map<String, String> frag = TestUtils.urlParameters(location);
		String code = frag.get("code");
		
		// Get id_token from authorization code
		user.setName("CN=myApp/O=APP");
		user.setCommon("myApp");
		result = this.mockMvc.perform(
				post("/token")
				.param("grant_type", "authorization_code")
				.param("code", code)
		).andExpect(status().is(200))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		Map<String, Object> tkRes = this.fromJson(json);
		
		assertThat(tkRes, hasEntry("scope", (Object) ""));		// No scopes granted
		assertThat(tkRes, not(hasKey("id_token")));
	}
}
