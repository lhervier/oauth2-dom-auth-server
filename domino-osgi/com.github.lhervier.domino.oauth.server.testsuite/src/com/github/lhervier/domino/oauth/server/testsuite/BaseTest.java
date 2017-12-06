package com.github.lhervier.domino.oauth.server.testsuite;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
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

import com.github.lhervier.domino.oauth.server.testsuite.impl.SecretRepositoryTestImpl;
import com.github.lhervier.domino.oauth.server.testsuite.impl.TimeServiceTestImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("test")
@WebAppConfiguration(value = "../com.github.lhervier.domino.oauth.server/")		// To access freemarkers templates
public abstract class BaseTest {

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
	private WebApplicationContext wac;
	
	@Autowired
	protected ObjectMapper mapper;
	
	protected MockMvc mockMvc;
	
	@Before
	public void baseSetUp() {
		mockMvc = webAppContextSetup(wac).build();
		TimeServiceTestImpl.CURRENT_TIME = System.currentTimeMillis() / 1000L;
		SecretRepositoryTestImpl.CRYPT_KEY = SecretRepositoryTestImpl.INITIAL_CRYPT_KEY;
		SecretRepositoryTestImpl.SIGN_KEY = SecretRepositoryTestImpl.INITIAL_SIGN_KEY;
	}
	
	@SuppressWarnings("unchecked")
	protected Map<String, Object> fromJson(String json) throws JsonParseException, JsonMappingException, IOException {
		return this.mapper.readValue(json, Map.class);
	}
	
	
}
