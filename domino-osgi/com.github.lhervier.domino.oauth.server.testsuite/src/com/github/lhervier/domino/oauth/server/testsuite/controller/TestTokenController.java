package com.github.lhervier.domino.oauth.server.testsuite.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.lhervier.domino.oauth.server.NotesPrincipal.AuthType;
import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.repo.ApplicationRepository;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.NotesPrincipalTestImpl;
import com.github.lhervier.domino.oauth.server.testsuite.TestConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("test")
public class TestTokenController extends BaseTest {

	public static final String APP_CLIENTID = "1234";
	public static final String APP_NAME = "myApp";
	public static final String APP_FULLNAME = "CN=myApp/O=APPLICATION";
	public static final String APP_REDIRECTURI = "http://acme.com/myApp";
	
	@Autowired
	private NotesPrincipalTestImpl user;
	
	@Autowired
	private ApplicationRepository appRepo;
	
	@Before
	public void setUp() {
		reset(appRepo);
		
		when(appRepo.findOneByName("myApp")).thenReturn(new ApplicationEntity() {{
			setClientId(APP_CLIENTID);
			setName(APP_NAME);
			setFullName(APP_FULLNAME);
			setRedirectUri(APP_REDIRECTURI);
			setRedirectUris(new ArrayList<String>());
		}});
		
		user.setAuthType(AuthType.NOTES);
		user.setName(APP_FULLNAME);
		user.setCommon(APP_NAME);
		user.setRoles(new ArrayList<String>());
		user.setClientId(null);
		user.setScopes(null);
		user.setCurrentDatabasePath(this.oauth2Db);
	}
	
	/**
	 * User must not be authenticated using bearer tokens
	 */
	@Test
	public void notUsingBearerAuth() throws Exception {
		user.setAuthType(AuthType.BEARER);
		
		mockMvc
		.perform(post("/token"))
		.andExpect(status().is(401));
	}
	
	/**
	 * User must be logged in as an application
	 */
	@Test
	public void notLoggedInAsApp() throws Exception {
		user.setName("CN=Lionel/O=USER");
		user.setCommon("Lionel");
		
		this.mockMvc
				.perform(post("/token"))
				.andExpect(status().is(403));
	}
	
	/**
	 * User must be logged at the root of the oauth2 application
	 */
	@Test
	public void usingDbRoot() throws Exception {
		user.setCurrentDatabasePath(null);
		this.mockMvc
		.perform(post("/token"))
		.andExpect(status().is(404));
	}
	
	/**
	 * Controller must be called on oauth2 db path
	 */
	@Test
	public void notOnOauth2Db() throws Exception {
		user.setCurrentDatabasePath("otherdb.nsf");		// Other database
		
		mockMvc
		.perform(post("/token"))
		.andExpect(status().is(404));
	}
	
	/**
	 * client_id (if passed) must be coherent with the logged in app
	 */
	@Test
	public void incoherentClientId() throws Exception {
		this.mockMvc
		.perform(
				post("/token")
				.param("client_id", "not1234")
		).andExpect(status().is(400))
		.andExpect(content().string(containsString("invalid client_id")));
	}
	
	/**
	 * grant_type is mandatory
	 */
	@Test
	public void noGrantType() throws Exception {
		this.mockMvc
		.perform(post("/token"))		// logged in as application 1234
		.andExpect(status().is(400))
		.andExpect(content().string(containsString("grant_type is mandatory")));
	}
	
	/**
	 * Unknown grant type
	 */
	@Test
	public void unknownGrantType() throws Exception {
		this.mockMvc
		.perform(
				post("/token")
				.param("grant_type", "unknown")
		).andExpect(status().is(400))
		.andExpect(content().string(containsString("unknown grant_type")));
	}
	
	/**
	 * Run dummy grant
	 */
	@Test
	public void dummyGrant() throws Exception {
		this.mockMvc
		.perform(
				post("/token")
				.param("grant_type", "dummy_grant")
		).andExpect(status().is(200))
		.andExpect(content().string(containsString("12345")));
	}
}
