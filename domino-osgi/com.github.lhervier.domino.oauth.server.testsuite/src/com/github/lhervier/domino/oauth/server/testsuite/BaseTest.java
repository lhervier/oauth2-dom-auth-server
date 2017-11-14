package com.github.lhervier.domino.oauth.server.testsuite;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.github.lhervier.domino.oauth.server.NotesPrincipal.AuthType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("test")
@WebAppConfiguration(value = "../com.github.lhervier.domino.oauth.server/")		// To access freemarkers templates
public class BaseTest {

	@Value("${oauth2.server.db}")
	private String oauth2Db;
	
	@Autowired
	protected NotesPrincipalTestImpl user;
	
	@Autowired
	private WebApplicationContext wac;
	
	protected MockMvc mockMvc;
	
	@Before
	public void baseSetUp() {
		mockMvc = webAppContextSetup(wac).build();
		
		user.setAuthType(AuthType.NOTES);
		user.setName("CN=Lionel/O=USER");
		user.setCommon("Lionel");
		user.setRoles(new ArrayList<String>());
		user.getRoles().add("[AppsManager]");
		user.setClientId(null);
		user.setScopes(null);
		user.setCurrentDatabasePath(this.oauth2Db);
	}
}
