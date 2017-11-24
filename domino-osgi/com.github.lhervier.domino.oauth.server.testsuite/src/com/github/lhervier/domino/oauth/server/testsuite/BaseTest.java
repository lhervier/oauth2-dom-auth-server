package com.github.lhervier.domino.oauth.server.testsuite;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.codehaus.jackson.map.ObjectMapper;
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

import com.github.lhervier.domino.oauth.server.repo.SecretRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("test")
@WebAppConfiguration(value = "../com.github.lhervier.domino.oauth.server/")		// To access freemarkers templates
public class BaseTest {

	@Value("${oauth2.server.db}")
	protected String oauth2Db;
	
	@Value("${oauth2.server.nab}")
	protected String oauth2Nab;
	
	@Value("${oauth2.server.applicationRoot}")
	protected String appRoot;
	
	@Value("${oauth2.server.refreshTokenConfig}")
	protected String refreshTokenConfig;
	
	@Value("${oauth2.server.refreshTokenLifetime}")
	protected long refreshTokenLifetime;
	
	@Value("${oauth2.server.authCodeLifetime}")
	protected long authCodeLifetime;
	
	@Value("${oauth2.server.core.signKey}")
	protected String coreSignKey;
	
	@Value("${oauth2.server.core.iss}")
	protected String coreIss;
	
	@Value("${oauth2.server.core.expiresIn}")
	protected long coreExpiresIn;
	
	@Value("${oauth2.server.openid.signKey}")
	protected String openidSignKey;
	
	@Value("${oauth2.server.openid.iss}")
	protected String openidIss;
	
	@Value("${oauth2.server.openid.expiresIn}")
	protected long openidExpiresIn;
	
	@Autowired
	protected SecretRepository secretRepo;
	
	@Autowired
	private WebApplicationContext wac;
	
	protected MockMvc mockMvc;
	
	protected ObjectMapper mapper = new ObjectMapper();
	
	@Before
	public void baseSetUp() {
		mockMvc = webAppContextSetup(wac).build();
	}
}
