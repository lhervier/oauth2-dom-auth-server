package com.github.lhervier.domino.oauth.server.testsuite;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.OauthServerConfig;
import com.github.lhervier.domino.oauth.server.WebConfig;
import com.github.lhervier.domino.oauth.server.repo.ApplicationRepository;
import com.github.lhervier.domino.oauth.server.repo.PersonRepository;
import com.github.lhervier.domino.spring.servlet.SpringServletConfig;

@Configuration
@Import(value = {SpringServletConfig.class, OauthServerConfig.class, WebConfig.class})
@PropertySource(value = "classpath:/test.properties")
public class TestConfig {

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
	public NotesPrincipal notesPrincipal() {
		return new NotesPrincipalTestImpl();
	}
}
