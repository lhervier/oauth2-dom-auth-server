package com.github.lhervier.domino.oauth.server.testsuite.ext;

import static com.github.lhervier.domino.oauth.server.testsuite.utils.TestUtils.urlRefs;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import com.github.lhervier.domino.oauth.server.NotesPrincipal.AuthType;
import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.ext.core.AccessToken;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.impl.NotesPrincipalTestImpl;

public class TestImplicitGrantFlow extends BaseTest {

	@Autowired
	protected NotesPrincipalTestImpl user;
	
	@Before
	public void before() {
		when(extSvcMock.getResponseTypes()).thenCallRealMethod();
		when(extSvcMock.getExtension(anyString())).thenCallRealMethod();
		
		when(this.appRepoMock.findOne(eq("1234"))).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp/O=APP");
			setRedirectUri("http://acme.com/myApp");
		}});
		
		user.setAuthType(AuthType.NOTES);
		user.setName("CN=Lionel/O=USER");
		user.setCommon("Lionel");
		user.setClientId(null);
		user.setScopes(null);
		user.setCurrentDatabasePath(this.oauth2Db);
	}
	
	/**
	 * Ask for an access token
	 */
	@Test
	public void whenAskForToken_thenReturnTokenInFragment() throws Exception {
		MvcResult result = this.mockMvc.perform(
				get("/authorize")
				.param("response_type", "token")
				.param("client_id", "1234")
				.param("redirect_uri", "http://acme.com/myApp")
		)
		.andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		assertThat(location, startsWith("http://acme.com/myApp#"));
		Map<String, String> frag = urlRefs(location);
		assertThat(frag, hasKey("access_token"));
		assertThat(frag, hasKey("token_type"));
		assertThat(frag, not(hasKey("refresh_token")));
		assertThat(frag, not(hasKey("code")));
		
		String tk = frag.get("access_token");
		AccessToken token = this.jwtSvc.fromJws(tk, coreSignKey, AccessToken.class);
		assertThat(token, notNullValue());
		assertThat(token.getSub(), equalTo("CN=Lionel/O=USER"));
		assertThat(token.getAud(), equalTo("1234"));
		assertThat(token.getExpires(), equalTo(timeSvc.currentTimeSeconds() + coreExpiresIn));
	}
}
