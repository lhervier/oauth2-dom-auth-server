package com.github.lhervier.domino.oauth.server.testsuite.ext;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import com.github.lhervier.domino.oauth.server.NotesPrincipal.AuthType;
import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.entity.PersonEntity;
import com.github.lhervier.domino.oauth.server.ext.openid.IdToken;
import com.github.lhervier.domino.oauth.server.model.ClientType;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.impl.NotesPrincipalTestImpl;

public class TestOpenIdUserInfoEndPoint extends BaseTest {

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
	}
	
	/**
	 * When called without access token, then 401
	 */
	@Test
	public void whenCalledWithNoAccessToken_then401() throws Exception {
		user.setAuthType(AuthType.NOTES);
		user.setName(null);
		user.setCommon(null);
		user.setClientId(null);
		user.setScopes(null);
		user.setCurrentDatabasePath(null);
		
		mockMvc.perform(get("/userInfo"))
		.andExpect(status().is(401));
	}
	
	/**
	 * When logged in as notes user, then 401
	 */
	@Test
	public void whenLoggedInWithNotes_then401() throws Exception {
		user.setAuthType(AuthType.NOTES);
		user.setName("CN=Lionel/O=USER");
		user.setCommon("Lionel");
		user.setClientId(null);
		user.setScopes(null);
		user.setCurrentDatabasePath(null);
		
		mockMvc.perform(get("/userInfo"))
		.andExpect(status().is(401));
	}
	
	/**
	 * When logged in as app, then 403
	 */
	@Test
	public void whenLoggedInAsApp_then403() throws Exception {
		user.setAuthType(AuthType.BEARER);
		user.setName("CN=myApp/O=APP");
		user.setCommon("myApp");
		user.setClientId(null);
		user.setScopes(null);
		user.setCurrentDatabasePath(null);
		
		ApplicationEntity app = new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp/O=APP");
			setRedirectUri("http://acme.com/myApp");
			setClientType(ClientType.CONFIDENTIAL.name());
		}};
		when(this.appRepoMock.findOne(eq("1234"))).thenReturn(app);
		when(this.appRepoMock.findOneByName(eq("myApp"))).thenReturn(app);
		
		mockMvc.perform(get("/userInfo"))
		.andExpect(status().is(403));
	}
	
	/**
	 * When not logged on serveur root, then 404
	 */
	@Test
	public void whenNotLoggedOnServerRoot_then404() throws Exception {
		user.setAuthType(AuthType.BEARER);
		user.setName("CN=Lionel/O=USER");
		user.setCommon("Lionel");
		user.setClientId(null);
		user.setScopes(null);
		user.setCurrentDatabasePath(this.oauth2Db);
		
		mockMvc.perform(get("/userInfo"))
		.andExpect(status().is(404));
	}
	
	/**
	 * Extract id token
	 */
	@Test
	public void whenLoggedInCorrectly_thenReturnIdtoken() throws Exception {
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
		when(this.personRepoMock.findOne("CN=Lionel/O=USER")).thenReturn(new PersonEntity() {{
			setFirstName("Lionel");
			setLastName("HERVIER");
			setFullNames(Arrays.asList("CN=Lionel/O=USER"));
			setInternetAddress("lhervier@asi.fr");
			setMiddleInitial("jr");
			setOfficePhoneNumber("+33412345678");
			setPhotoUrl("http://acme.com/photos/lionel");
			setShortName("lio");
			setTitle("Mr");
			setWebsite("https://github.com/lhervier");
		}});
		
		// Login as user, using bearer
		user.setAuthType(AuthType.BEARER);
		user.setName("CN=Lionel/O=USER");
		user.setCommon("Lionel");
		user.setClientId("1234");
		user.setScopes(Arrays.asList("openid", "profile", "email"));
		user.setCurrentDatabasePath(null);
		
		// Ask for id token
		MvcResult result = this.mockMvc.perform(get("/userInfo"))
		.andExpect(status().is(200))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		IdToken tk = this.mapper.readValue(json, IdToken.class);
		assertThat(tk.getSub(), equalTo("CN=Lionel/O=USER"));
		assertThat(tk.getEmail(), equalTo("lhervier@asi.fr"));
		assertThat(tk.getPhoneNumber(), nullValue());		// Only profile and email scopes are granted. Not phone.
		assertThat(tk.getNonce(), nullValue());				// No nonce on userInfo endpoint
	}
}