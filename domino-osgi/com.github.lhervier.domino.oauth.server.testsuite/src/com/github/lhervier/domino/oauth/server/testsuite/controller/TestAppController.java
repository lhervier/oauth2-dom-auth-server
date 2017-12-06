package com.github.lhervier.domino.oauth.server.testsuite.controller;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.lhervier.domino.oauth.server.NotesPrincipal.AuthType;
import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.impl.NotesPrincipalTestImpl;

public class TestAppController extends BaseTest {

	@Autowired
	protected NotesPrincipalTestImpl user;
	
	@Before
	public void setUp() {
		user.setAuthType(AuthType.NOTES);
		user.setName("CN=Lionel/O=USER");
		user.setCommon("Lionel");
		user.setRoles(Arrays.asList("[AppsManager]"));
		user.setClientId(null);
		user.setScopes(null);
		user.setCurrentDatabasePath(this.oauth2Db);
	}
	
	@Test
	public void whenAppsManager_thenOK() throws Exception {
		mockMvc
		.perform(get("/html/listApplications"))
		.andExpect(status().is(200));
	}
	
	/**
	 * User must have the [AppsManager] role
	 */
	@Test
	public void whenNotAppsManager_then403() throws Exception {
		user.setRoles(new ArrayList<String>());		// No roles
		
		mockMvc
		.perform(get("/html/listApplications"))
		.andExpect(status().is(403));
	}
	
	/**
	 * User must not be authenticated using bearer tokens
	 */
	@Test
	public void whenBearerAuth_then401() throws Exception {
		user.setAuthType(AuthType.BEARER);
		
		mockMvc
		.perform(get("/html/listApplications"))
		.andExpect(status().is(401));
	}
	
	/**
	 * User cannot be logged in as an application
	 */
	@Test
	public void whenLoggedAsAnApplication_then403() throws Exception {
		when(appRepoMock.findOneByName(eq("Lionel"))).thenReturn(new ApplicationEntity() {{
			setClientId("567890");
			setName("Lionel");
			setFullName("CN=Lionel/O=USER");
			setRedirectUri("http://acme.com/lionel");
		}});
		
		mockMvc
		.perform(get("/html/listApplications"))
		.andExpect(status().is(403));
	}
	
	/**
	 * Controller must be called on oauth2 db path
	 */
	@Test
	public void whenNotUsingOauth2Db_then404() throws Exception {
		user.setCurrentDatabasePath("otherdb.nsf");		// Other database
		
		mockMvc
		.perform(get("/html/listApplications"))
		.andExpect(status().is(404));
	}
	
	/**
	 * Controller must not be called on the server root
	 */
	@Test
	public void whenUsingServerRoot_then404() throws Exception {
		user.setCurrentDatabasePath(null);
		
		mockMvc
		.perform(get("/html/listApplications"))
		.andExpect(status().is(404));
	}
}
