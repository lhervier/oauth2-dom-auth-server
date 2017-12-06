package com.github.lhervier.domino.oauth.server.testsuite.controller;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.hamcrest.collection.IsMapContaining;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import com.github.lhervier.domino.oauth.server.NotesPrincipal.AuthType;
import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.ext.core.AccessToken;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.impl.NotesPrincipalTestImpl;

public class TestCheckTokenController extends BaseTest {

	@Autowired
	private NotesPrincipalTestImpl user;
	
	@Before
	public void setUp() {
		when(appRepoMock.findOneByName("myApp")).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp/O=APP");
			setRedirectUri("http://acme.com/myApp");
		}});
		
		user.setAuthType(AuthType.NOTES);
		user.setCommon("myApp");
		user.setCurrentDatabasePath(this.oauth2Db);
		user.setName("CN=myApp/O=APP");
	}
	
	/**
	 * when scopes in access token, then scopes in response
	 */
	@Test
	public void whenScopeInAccessToken_thenScopeInResponse() throws Exception {
		AccessToken accessToken = new AccessToken();
		accessToken.setAud("1234");
		accessToken.setExpires(timeSvc.currentTimeSeconds() + 10L);
		accessToken.setIss(coreIss);
		accessToken.setSub("CN=Lionel/O=USER");
		accessToken.setScope("scope1 scope2");
		
		MvcResult result = this.mockMvc.perform(
				post("/checkToken")
				.param("token", this.jwtSvc.createJws(accessToken, this.coreSignKey))
		).andExpect(status().is(200))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		Map<String, Object> resp = this.fromJson(json);
		
		assertThat(resp, IsMapContaining.hasEntry("scope", (Object) "scope1 scope2"));
	}
}
