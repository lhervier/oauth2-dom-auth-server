package com.github.lhervier.domino.oauth.server.testsuite.controller;

import static com.github.lhervier.domino.oauth.server.testsuite.impl.DummyExt.DUMMY_SCOPE;
import static com.github.lhervier.domino.oauth.server.testsuite.utils.TestUtils.urlParameters;
import static com.github.lhervier.domino.oauth.server.testsuite.utils.TestUtils.urlRefs;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import com.github.lhervier.domino.oauth.server.NotesPrincipal.AuthType;
import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ext.core.CoreContext;
import com.github.lhervier.domino.oauth.server.ext.core.CoreExt;
import com.github.lhervier.domino.oauth.server.repo.ApplicationRepository;
import com.github.lhervier.domino.oauth.server.repo.AuthCodeRepository;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.impl.DummyExt;
import com.github.lhervier.domino.oauth.server.testsuite.impl.NotesPrincipalTestImpl;
import com.github.lhervier.domino.oauth.server.testsuite.impl.TimeServiceTestImpl;

public class TestAuthorizeController extends BaseTest {

	@Autowired
	private ApplicationRepository appRepoMock;
	
	@Autowired
	private AuthCodeRepository authCodeRepoMock;
	
	@Autowired
	private CoreExt coreExt;
	
	@Autowired
	private DummyExt dummyExt;
	
	@Autowired
	private NotesPrincipalTestImpl user;
	
	@Before
	public void setUp() {
		reset(appRepoMock);
		reset(authCodeRepoMock);
		
		user.setAuthType(AuthType.NOTES);
		user.setName("CN=Lionel/O=USER");
		user.setCommon("Lionel");
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
		.perform(get("/authorize"))
		.andExpect(status().is(401));
	}
	
	/**
	 * User cannot be logged in as an application
	 */
	@Test
	public void notLoggedAsAnApplication() throws Exception {
		when(appRepoMock.findOneByName(eq("Lionel"))).thenReturn(new ApplicationEntity() {{
			setClientId("567890");
			setName("Lionel");
			setFullName("CN=Lionel/O=USER");
			setRedirectUri("http://acme.com/lionel");
		}});
		
		mockMvc
		.perform(get("/authorize"))
		.andExpect(status().is(403));
	}
	
	/**
	 * Controller must be called on oauth2 db path
	 */
	@Test
	public void notOnOauth2Db() throws Exception {
		user.setCurrentDatabasePath("otherdb.nsf");		// Other database
		
		mockMvc
		.perform(get("/authorize"))
		.andExpect(status().is(404));
	}
	
	/**
	 * Controller must not be called on the server root
	 */
	@Test
	public void onServerRoot() throws Exception {
		user.setCurrentDatabasePath(null);
		
		mockMvc
		.perform(get("/authorize"))
		.andExpect(status().is(404));
	}
	
	// ====================================================================================================
	
	/**
	 * client_id is mandatory
	 */
	@Test
	public void noClientId() throws Exception {
		mockMvc
		.perform(get("/authorize"))
		.andExpect(status().is(500))
		.andExpect(content().string(containsString("client_id is mandatory")));
	}
	
	/**
	 * client_id is mandatory
	 */
	@Test
	public void emptyClientId() throws Exception {
		mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "")
		)
		.andExpect(status().is(500))
		.andExpect(content().string(containsString("client_id is mandatory")));
	}
	
	/**
	 * client_id must match with an existing app
	 */
	@Test
	public void invalidClientId() throws Exception {
		mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "invalid_value")
		)
		.andExpect(status().is(500))
		.andExpect(content().string(containsString("client_id is invalid")));
	}
	
	// =======================================================================
	
	/**
	 * No redirect_uri, and value not defined in app
	 */
	@Test
	public void noRedirectUriEvenInApp() throws Exception {
		when(appRepoMock.findOne(anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setRedirectUri(null);
		}});
		mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
		)
		.andExpect(status().is(500))
		.andExpect(content().string(containsString("redirect_uri is mandatory")));
	}
	
	/**
	 * No redirect uri, but app only have one
	 */
	@Test
	public void noRedirectUriButAppOnlyHaveOne() throws Exception {
		when(appRepoMock.findOne(eq("1234"))).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setRedirectUri("http://acme.com/myApp");
		}});
		MvcResult result = mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "token")
		)
		.andExpect(status().is(302))
		.andReturn();
		String location = result.getResponse().getHeader("Location");
		assertThat(location, not(equalTo("error")));
	}
	
	/**
	 * Invalid redirect_uri
	 */
	@Test
	public void invalidRedirectUri() throws Exception {
		when(appRepoMock.findOne(Mockito.anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setRedirectUri("http://acme.com/myApp");
		}});
		mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("redirect_uri", "http://acme.com/otherApp")
		)
		.andExpect(status().is(500))
		.andExpect(content().string(containsString("redirect_uri is invalid")));
	}
	
	/**
	 * RedirectUri is one of the additionnal URIs
	 */
	@Test
	public void usingAdditionalRedirectUri() throws Exception {
		when(appRepoMock.findOne(eq("1234"))).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setRedirectUri("http://acme.com/myApp");
			setRedirectUris(Arrays.asList("http://acme.com/otherUri"));
		}});
		MvcResult result = mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("redirect_uri", "http://acme.com/otherUri")
				.param("response_type", "token")
		)
		.andExpect(status().is(302))
		.andReturn();
		String location = result.getResponse().getHeader("Location");
		assertThat(location, not(equalTo("error")));
	}
	
	/**
	 * No redirect uri, and app have multiple
	 */
	@Test
	public void noRedirectUriAndAppHaveMultiple() throws Exception {
		when(appRepoMock.findOne(Mockito.anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setRedirectUri("http://acme.com/myApp");
			setRedirectUris(Arrays.asList("http://acme.com/other"));
		}});
		mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
		)
		.andExpect(status().is(500))
		.andExpect(content().string(containsString("redirect_uri is mandatory")));
	}
	
	// ========================================================================
	
	/**
	 * No response_type
	 */
	@Test
	public void noResponseType() throws Exception {
		when(appRepoMock.findOne(Mockito.anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setRedirectUri("http://acme.com/myApp");
		}});
		MvcResult result = mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
		)
		.andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		assertThat(location, startsWith("http://acme.com/myApp"));
		
		Map<String, String> params = urlParameters(location);
		assertThat(params, hasEntry("error", "invalid_request"));
		assertThat(params, hasKey("error_uri"));
		assertThat(params, hasKey("error_description"));
	}
	
	/**
	 * Empty response_type
	 */
	@Test
	public void emptyResponseType() throws Exception {
		when(appRepoMock.findOne(Mockito.anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setRedirectUri("http://acme.com/myApp");
		}});
		MvcResult result = mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "")
		)
		.andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		assertThat(location, startsWith("http://acme.com/myApp"));
		
		Map<String, String> params = urlParameters(location);
		assertThat(params, hasEntry("error", "invalid_request"));
		assertThat(params, hasKey("error_uri"));
		assertThat(params, hasKey("error_description"));
	}
	
	/**
	 * Invalid response type
	 */
	@Test
	public void invalidResponseType() throws Exception {
		when(appRepoMock.findOne(Mockito.anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setRedirectUri("http://acme.com/myApp");
		}});
		MvcResult result = mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "azerty")
		)
		.andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		assertThat(location, startsWith("http://acme.com/myApp"));
		
		Map<String, String> params = urlParameters(location);
		assertThat(params, hasEntry("error", "invalid_request"));
		assertThat(params, hasKey("error_uri"));
		assertThat(params, hasKey("error_description"));
	}
	
	// =======================================================================
	
	/**
	 * Get an authorization code without a redirect uri.
	 * App only have one, so it's ok.
	 */
	@Test
	public void authCodeFlowNoRedirectUri() throws Exception {
		when(appRepoMock.findOne(anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp" + appRoot);
			setRedirectUri("http://acme.com/myApp");
		}});
		
		when(authCodeRepoMock.save(any(AuthCodeEntity.class))).then(returnsFirstArg());
		
		MvcResult result = mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "code")
				.param("state", "myState")
				.param("scope", "azerty uiop")
		)
		.andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		assertThat(location, startsWith("http://acme.com/myApp"));
		
		Map<String, String> params = urlParameters(location);
		assertThat(params, hasKey("code"));
		assertThat(params, hasEntry("state", "myState"));
		
		ArgumentCaptor<AuthCodeEntity> authCodeCaptor = ArgumentCaptor.forClass(AuthCodeEntity.class);
		Mockito.verify(authCodeRepoMock, Mockito.times(1)).save(authCodeCaptor.capture());
		List<AuthCodeEntity> added = authCodeCaptor.getAllValues();
		assertThat(added.size(), is(equalTo(1)));
		AuthCodeEntity code = added.get(0);
		
		assertThat(code.getId(), equalTo(params.get("code")));
		
		assertThat(code.getRedirectUri(), equalTo("http://acme.com/myApp"));
		assertThat(code.getClientId(), equalTo("1234"));
		assertThat(code.getExpires(), equalTo(TimeServiceTestImpl.CURRENT_TIME + authCodeLifetime));
		
		assertThat(code.getScopes(), containsInAnyOrder("azerty", "uiop"));
		assertThat(code.getGrantedScopes(), emptyIterable());
		
		assertThat(code.getContextClasses().size(), is(equalTo(2)));
		assertThat(code.getContextClasses(), hasKey(this.coreExt.getId()));
		assertThat(code.getContextClasses(), hasKey(this.dummyExt.getId()));
		
		String jsonCtx = code.getContextObjects().get(this.coreExt.getId());
		ObjectMapper mapper = new ObjectMapper();
		CoreContext ctx = mapper.readValue(jsonCtx, CoreContext.class);
		
		assertThat(ctx.getAud(), equalTo("1234"));
		assertThat(ctx.getIss(), equalTo(coreIss));
		assertThat(ctx.getSub(), equalTo("CN=Lionel/O=USER"));
	}
	
	/**
	 * Additional parameters in redirect uri.
	 * https://tools.ietf.org/html/rfc6749#section-3.1
	 */
	@Test
	public void existingParamsInRedirectUri() throws Exception {
		when(appRepoMock.findOne(anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp" + appRoot);
			setRedirectUri("http://acme.com/myApp?param1=xxx");			// Existing parameters in uri
		}});
		
		when(authCodeRepoMock.save(any(AuthCodeEntity.class))).then(returnsFirstArg());
		
		MvcResult result = mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "code")
		)
		.andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		assertThat(location, startsWith("http://acme.com/myApp"));
		
		Map<String, String> params = urlParameters(location);
		assertThat(params, hasKey("code"));
		assertThat(params, hasEntry("param1", "xxx"));
	}
	
	/**
	 * Fragment in redirect_uri, and response_type = code
	 * https://tools.ietf.org/html/rfc6749#section-3.1
	 */
	@Test
	public void fragmentInCodeResponseType() throws Exception {
		when(appRepoMock.findOne(eq("1234"))).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp" + appRoot);
			setRedirectUri("http://acme.com/myApp#param1=xxx");			// Fragment in uri
		}});
		mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "code")
		)
		.andExpect(status().is(500))
		.andExpect(content().string(containsString("invalid redirect_uri")));
	}
	
	/**
	 * Not all scopes grantes
	 */
	@Test
	public void dontGrantAllScopes() throws Exception {
		when(appRepoMock.findOne(anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp" + appRoot);
			setRedirectUri("http://acme.com/myApp");
		}});
		
		when(authCodeRepoMock.save(any(AuthCodeEntity.class))).then(returnsFirstArg());
		
		mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "code")
				.param("scope", DUMMY_SCOPE + " non_existing_scope")
		)
		.andExpect(status().is(302));
		
		ArgumentCaptor<AuthCodeEntity> authCodeCaptor = ArgumentCaptor.forClass(AuthCodeEntity.class);
		Mockito.verify(authCodeRepoMock, Mockito.times(1)).save(authCodeCaptor.capture());
		List<AuthCodeEntity> added = authCodeCaptor.getAllValues();
		assertThat(added.size(), is(equalTo(1)));
		AuthCodeEntity code = added.get(0);
		
		assertThat(code.getScopes(), containsInAnyOrder(DUMMY_SCOPE, "non_existing_scope"));
		assertThat(code.getGrantedScopes(), containsInAnyOrder(DUMMY_SCOPE));
	}
	
	// ======================================================================================
	
	/**
	 * Get an access token
	 */
	@Test
	public void tokenFlow() throws Exception {
		when(appRepoMock.findOne(anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp" + appRoot);
			setRedirectUri("http://acme.com/myApp");
		}});
		
		when(authCodeRepoMock.save(any(AuthCodeEntity.class))).then(returnsFirstArg());
				
		MvcResult result = mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "token")
				.param("state", "myState")
		)
		.andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		assertThat(location, startsWith("http://acme.com/myApp"));
		
		Map<String, String> params = urlRefs(location);
		assertThat(params, hasKey("access_token"));
		assertThat(params, hasEntry("state", "myState"));
		assertThat(params, not(hasKey("refresh_token")));
	}
	
	/**
	 * Additional parameters in redirect uri
	 * WARNING: RFC says that fragments are not allowed. 
	 * But our implementation allowed them when using "token" response type, because token is already sent in a fragment.
	 * https://tools.ietf.org/html/rfc6749#section-3.1
	 */
	@Test
	public void fragmentInTokenResponseType() throws Exception {
		when(appRepoMock.findOne(anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp" + appRoot);
			setRedirectUri("http://acme.com/myApp#param1=xxx");			// Existing parameters in uri
		}});
		
		when(authCodeRepoMock.save(any(AuthCodeEntity.class))).then(returnsFirstArg());
		
		MvcResult result = mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "token")
		)
		.andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		assertThat(location, startsWith("http://acme.com/myApp"));
		
		Map<String, String> params = urlRefs(location);
		assertThat(params, hasKey("access_token"));
		assertThat(params, hasEntry("param1", "xxx"));
	}
}
