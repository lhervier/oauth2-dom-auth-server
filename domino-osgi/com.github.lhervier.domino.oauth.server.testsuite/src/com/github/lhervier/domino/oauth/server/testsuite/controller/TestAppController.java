package com.github.lhervier.domino.oauth.server.testsuite.controller;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.github.lhervier.domino.oauth.server.NotesPrincipal.AuthType;
import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.repo.ApplicationRepository;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.TestConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("test")
@WebAppConfiguration(value = "../com.github.lhervier.domino.oauth.server/")		// To access freemarkers templates
public class TestAppController extends BaseTest {

	@Autowired
	private ApplicationRepository appRepoMock;
	
	@Before
	public void setUp() {
		reset(appRepoMock);
	}
	
	@Test
	public void userIsOk() throws Exception {
		mockMvc
		.perform(get("/html/listApplications"))
		.andExpect(status().is(200));
	}
	
	/**
	 * User must have the [AppsManager] role
	 */
	@Test
	public void dontHaveAppsManagerRole() throws Exception {
		user.setRoles(new ArrayList<String>());		// No roles
		
		mockMvc
		.perform(get("/html/listApplications"))
		.andExpect(status().is(403));
	}
	
	/**
	 * User must not be authenticated using bearer tokens
	 */
	@Test
	public void usingBearerAuth() throws Exception {
		user.setAuthType(AuthType.BEARER);
		
		mockMvc
		.perform(get("/html/listApplications"))
		.andExpect(status().is(401));
	}
	
	/**
	 * User cannot be logged in as an application
	 */
	@Test
	public void loggedAsAnApplication() throws Exception {
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
	public void notUsingOauth2Db() throws Exception {
		user.setCurrentDatabasePath("otherdb.nsf");		// Other database
		
		mockMvc
		.perform(get("/html/listApplications"))
		.andExpect(status().is(404));
	}
	
	/**
	 * Controller must not be called on the server root
	 */
	@Test
	public void usingServerRoot() throws Exception {
		user.setCurrentDatabasePath(null);
		
		mockMvc
		.perform(get("/html/listApplications"))
		.andExpect(status().is(404));
	}
}
