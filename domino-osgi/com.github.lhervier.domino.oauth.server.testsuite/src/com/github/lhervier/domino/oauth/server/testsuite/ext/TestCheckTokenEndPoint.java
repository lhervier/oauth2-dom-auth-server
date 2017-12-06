package com.github.lhervier.domino.oauth.server.testsuite.ext;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import com.github.lhervier.domino.oauth.server.NotesPrincipal.AuthType;
import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.entity.PersonEntity;
import com.github.lhervier.domino.oauth.server.model.ClientType;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.impl.NotesPrincipalTestImpl;

public class TestCheckTokenEndPoint extends BaseTest {

	@Autowired
	protected NotesPrincipalTestImpl user;
	
	@Before
	public void before() {
		// Mocking app repo
		ApplicationEntity app = new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp/O=APP");
			setRedirectUri("http://acme.com/myApp");
			setClientType(ClientType.CONFIDENTIAL.name());
		}};
		when(this.appRepoMock.findOne(eq("1234"))).thenReturn(app);
		when(this.appRepoMock.findOneByName(eq("myApp"))).thenReturn(app);
		
		// Mocking person repo
		when(this.personRepoMock.findOne("CN=Lionel HERVIER/O=USER")).thenReturn(new PersonEntity() {{
			setFirstName("Lionel");
			setLastName("HERVIER");
			setFullNames(Arrays.asList("CN=Lionel HERVIER/O=USER"));
			setInternetAddress("lhervier@asi.fr");
			setMiddleInitial("jr");
			setOfficePhoneNumber("+33412345678");
			setPhotoUrl("http://acme.com/photos/lionel");
			setShortName("lio");
			setTitle("Mr");
			setWebsite("https://github.com/lhervier");
		}});
		
		// Login as app
		user.setAuthType(AuthType.NOTES);
		user.setName("CN=myApp/O=APP");
		user.setCommon("myApp");
		user.setClientId(null);
		user.setScopes(null);
		user.setCurrentDatabasePath(this.oauth2Db);
	}
	
	/**
	 * Invalid token => Not active
	 */
	@Test
	public void whenInvalidToken_thenNotActive() throws Exception {
		MvcResult result = this.mockMvc.perform(
				post("/checkToken")
				.param("token", "azerty")
		).andExpect(status().is(200))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		Map<String, Object> resp = this.fromJson(json);
		
		assertThat(resp.get("active"), equalTo((Object) Boolean.FALSE));
	}
}
