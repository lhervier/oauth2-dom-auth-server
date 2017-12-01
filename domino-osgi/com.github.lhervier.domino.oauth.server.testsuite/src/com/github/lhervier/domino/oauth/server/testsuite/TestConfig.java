package com.github.lhervier.domino.oauth.server.testsuite;

import static org.mockito.Mockito.mock;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import com.github.lhervier.domino.oauth.server.OauthServerConfig;
import com.github.lhervier.domino.oauth.server.WebConfig;
import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.ext.core.CoreExt;
import com.github.lhervier.domino.oauth.server.repo.ApplicationRepository;
import com.github.lhervier.domino.oauth.server.repo.AuthCodeRepository;
import com.github.lhervier.domino.oauth.server.repo.PersonRepository;
import com.github.lhervier.domino.oauth.server.repo.SecretRepository;
import com.github.lhervier.domino.oauth.server.services.AuthCodeService;
import com.github.lhervier.domino.oauth.server.services.ExtensionService;
import com.github.lhervier.domino.oauth.server.services.TimeService;
import com.github.lhervier.domino.oauth.server.testsuite.impl.DummyExt;
import com.github.lhervier.domino.oauth.server.testsuite.impl.TimeServiceTestImpl;
import com.github.lhervier.domino.spring.servlet.SpringServletConfig;

@Configuration
@Import(value = {SpringServletConfig.class, OauthServerConfig.class, WebConfig.class})
@PropertySource(value = "classpath:/test.properties")
public class TestConfig {

	@Autowired
	private CoreExt coreExt;
	
	@Autowired
	private DummyExt dummyExt;
	
	@Bean
	@Primary
	public PersonRepository personRespository() {
		return mock(PersonRepository.class);
	}

	@Bean
	@Primary
	public ApplicationRepository applicationRepository() {
		return mock(ApplicationRepository.class);
	}
	
	@Bean
	@Primary
	public AuthCodeRepository authCodeRepository() {
		return mock(AuthCodeRepository.class);
	}
	
	@Bean
	@Primary
	public TimeService timeService() {
		return new TimeServiceTestImpl();
	}
	
	@Bean
	@Primary
	public AuthCodeService AuthCodeService() {
		return mock(AuthCodeService.class);
	}
	
	@Bean
	@Primary
	public SecretRepository secretRepository() {
		return new SecretRepository() {
			@Override
			public byte[] findCryptSecret(String ssoConfig) {
				try {
					return "0123456789012345".getBytes("UTF-8");
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public byte[] findSignSecret(String ssoConfig) {
				try {
					return "98765432109876543210987654321098".getBytes("UTF-8");
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}
	
	@Bean
	@Primary
	public ExtensionService extensionService() {
		return new ExtensionService() {
			@SuppressWarnings("unchecked")
			@Override
			public List<? extends IOAuthExtension<?>> getExtensions() {
				return Arrays.asList(coreExt, dummyExt);
			}
		};
	}
}
