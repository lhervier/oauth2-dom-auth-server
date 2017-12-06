package com.github.lhervier.domino.oauth.server.testsuite.controller;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.NotesPrincipal.AuthType;
import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ext.IAuthorizer;
import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.repo.ApplicationRepository;
import com.github.lhervier.domino.oauth.server.repo.AuthCodeRepository;
import com.github.lhervier.domino.oauth.server.services.ExtensionService;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.impl.DummyContext;
import com.github.lhervier.domino.oauth.server.testsuite.impl.NotesPrincipalTestImpl;
import com.github.lhervier.domino.oauth.server.testsuite.impl.TimeServiceTestImpl;

public class TestAuthorizeController extends BaseTest {

	@Autowired
	private ApplicationRepository appRepoMock;
	
	@Autowired
	private AuthCodeRepository authCodeRepoMock;
	
	@Autowired
	protected ExtensionService extSvcMock;
	
	@Autowired
	private NotesPrincipalTestImpl user;
	
	@Before
	public void setUp() {
		reset(appRepoMock);
		reset(authCodeRepoMock);
		reset(extSvcMock);
		
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
	public void whenUsingBearerAuth_then401() throws Exception {
		user.setAuthType(AuthType.BEARER);
		
		mockMvc
		.perform(get("/authorize"))
		.andExpect(status().is(401));
	}
	
	/**
	 * User cannot be logged in as an application
	 */
	@Test
	public void whenNotLoggedAsAnApplication_then403() throws Exception {
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
	public void whenNotOnOauth2Db_then404() throws Exception {
		user.setCurrentDatabasePath("otherdb.nsf");		// Other database
		
		mockMvc
		.perform(get("/authorize"))
		.andExpect(status().is(404));
	}
	
	/**
	 * Controller must not be called on the server root
	 */
	@Test
	public void whenOnServerRoot_then404() throws Exception {
		user.setCurrentDatabasePath(null);
		
		mockMvc
		.perform(get("/authorize"))
		.andExpect(status().is(404));
	}
	
	// ====================================================================================================
	
	/**
	 * POST MAY be supported, and our implementation allows it.
	 * https://tools.ietf.org/html/rfc6749#section-3.1
	 */
	@Test
	public void whenUsingPost_thenOK() throws Exception {
		when(this.appRepoMock.findOne(eq("1234"))).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp/O=APP");
			setRedirectUri("http://acme.com/myApp");
		}});
		when(this.extSvcMock.getResponseTypes()).thenReturn(Arrays.asList("dummy"));
		when(this.extSvcMock.getExtension(eq("dummy"))).thenReturn(new IOAuthExtension() {
			public List<String> getAuthorizedScopes() { return new ArrayList<String>(); }
			public void token(NotesPrincipal user, Application app, Object context, List<String> askedScopes, IPropertyAdder adder) { }
			@Override
			public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, List<String> responseTypes, IAuthorizer authorizer) {
				authorizer.addCodeToResponse(true);		// Will save the auth code
				authorizer.addProperty("dummy_authorize_param", "authparamvalue");
			}
		});
		when(authCodeRepoMock.save(any(AuthCodeEntity.class))).then(returnsFirstArg());
		MvcResult result = mockMvc.perform(
				post("/authorize")
				.param("client_id", "1234")
				.param("response_type", "dummy")
		).andExpect(status().is(302)).andReturn();
		String location = result.getResponse().getHeader("Location");
		Map<String, String> params = urlParameters(location);
		assertThat(params, hasKey("code"));
		assertThat(params, hasEntry("dummy_authorize_param", "authparamvalue"));
		assertThat(params, not(hasKey("error")));
	}
	
	/**
	 * client_id is mandatory
	 */
	@Test
	public void whenNoClientId_then500() throws Exception {
		mockMvc
		.perform(get("/authorize"))
		.andExpect(status().is(500))
		.andExpect(content().string(containsString("client_id is mandatory")));
	}
	
	/**
	 * client_id is mandatory
	 */
	@Test
	public void whenEmptyClientId_then500() throws Exception {
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
	public void whenInvalidClientId_then500() throws Exception {
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
	 * https://tools.ietf.org/html/rfc6749#section-3.1.2.3
	 */
	@Test
	public void whenNoRedirectUriEvenInApp_then500() throws Exception {
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
	 * https://tools.ietf.org/html/rfc6749#section-3.1.2.3
	 */
	@Test
	public void whenNoRedirectUriButAppOnlyHaveOne_thenOK() throws Exception {
		when(appRepoMock.findOne(eq("1234"))).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setRedirectUri("http://acme.com/myApp");
		}});
		when(this.extSvcMock.getResponseTypes()).thenReturn(Arrays.asList("dummy"));
		when(this.extSvcMock.getExtension(eq("dummy"))).thenReturn(new IOAuthExtension() {
			public List<String> getAuthorizedScopes() { return new ArrayList<String>(); }
			public void token(NotesPrincipal user, Application app, Object context, List<String> askedScopes, IPropertyAdder adder) { }
			public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, List<String> responseTypes, IAuthorizer authorizer) {
				authorizer.addCodeToResponse(true);		// Will save the auth code
				authorizer.addProperty("dummy_authorize_param", "authparamvalue");
			}
		});
		MvcResult result = mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "dummy")
		)
		.andExpect(status().is(302))
		.andReturn();
		String location = result.getResponse().getHeader("Location");
		assertThat(location, not(equalTo("error")));
	}
	
	/**
	 * Invalid redirect_uri
	 * https://tools.ietf.org/html/rfc6749#section-3.1.2.4
	 */
	@Test
	public void whenInvalidRedirectUri_then500() throws Exception {
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
		.andExpect(status().is(500))		// MUST NOT redirect !
		.andExpect(content().string(containsString("redirect_uri is invalid")));
	}
	
	/**
	 * RedirectUri is one of the additionnal URIs
	 * https://tools.ietf.org/html/rfc6749#section-3.1.2.3
	 */
	@Test
	public void whenRedirectUriIsOneOfAdditionalRedirectUri_thenOK() throws Exception {
		when(appRepoMock.findOne(eq("1234"))).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setRedirectUri("http://acme.com/myApp");
			setRedirectUris(Arrays.asList("http://acme.com/otherUri"));
		}});
		when(this.extSvcMock.getResponseTypes()).thenReturn(Arrays.asList("dummy"));
		when(this.extSvcMock.getExtension(eq("dummy"))).thenReturn(new IOAuthExtension() {
			public List<String> getAuthorizedScopes() { return new ArrayList<String>(); }
			public void token(NotesPrincipal user, Application app, Object context, List<String> askedScopes, IPropertyAdder adder) { }
			public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, List<String> responseTypes, IAuthorizer authorizer) {
				authorizer.addCodeToResponse(true);		// Will save the auth code
				authorizer.addProperty("dummy_authorize_param", "authparamvalue");
			}
		});
		MvcResult result = mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("redirect_uri", "http://acme.com/otherUri")
				.param("response_type", "dummy")
		)
		.andExpect(status().is(302))
		.andReturn();
		String location = result.getResponse().getHeader("Location");
		assertThat(location, not(equalTo("error")));
	}
	
	/**
	 * No redirect uri, and app have multiple
	 * https://tools.ietf.org/html/rfc6749#section-3.1.2.3
	 */
	@Test
	public void whenNoRedirectUriAndAppHaveMultiple_thenError() throws Exception {
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
	 * https://tools.ietf.org/html/rfc6749#section-4.1.2.1
	 */
	@Test
	public void whenNoResponseType_thenError() throws Exception {
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
	 * https://tools.ietf.org/html/rfc6749#section-4.1.2.1
	 */
	@Test
	public void whenEmptyResponseType_thenError() throws Exception {
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
	 * https://tools.ietf.org/html/rfc6749#section-4.1.2.1
	 */
	@Test
	public void whenInvalidResponseType_thenError() throws Exception {
		when(appRepoMock.findOne(Mockito.anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setRedirectUri("http://acme.com/myApp");
		}});
		when(this.extSvcMock.getResponseTypes()).thenReturn(new ArrayList<String>());
		MvcResult result = mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "non_existing_response_type")
		)
		.andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		assertThat(location, startsWith("http://acme.com/myApp"));
		
		Map<String, String> params = urlParameters(location);
		assertThat(params, hasEntry("error", "unsupported_response_type"));
		assertThat(params, hasKey("error_uri"));
		assertThat(params, hasKey("error_description"));
	}
	
	/**
	 * NoOp response type
	 */
	@Test
	public void whenNoOpResponseType_thenNoCodeAndNoError() throws Exception {
		when(appRepoMock.findOne(Mockito.anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setRedirectUri("http://acme.com/myApp");
		}});
		when(this.extSvcMock.getResponseTypes()).thenReturn(Arrays.asList("noop"));
		when(this.extSvcMock.getExtension(eq("noop"))).thenReturn(new IOAuthExtension() {
			public List<String> getAuthorizedScopes() { return new ArrayList<String>(); }
			public void token(NotesPrincipal user, Application app, Object context, List<String> askedScopes, IPropertyAdder adder) { }
			public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, List<String> responseTypes, IAuthorizer authorizer) { }
		});
		MvcResult result = mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "noop")
		)
		.andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		assertThat(location, startsWith("http://acme.com/myApp"));
		
		Map<String, String> params = urlParameters(location);
		assertThat(params, not(hasKey("error")));
		assertThat(params, not(hasKey("code")));
	}
	
	/**
	 * Conflicting response types on auth code saving
	 */
	@Test
	public void whenConflictingResponseTypesOnAuthCodeSaving_thenError() throws Exception {
		when(appRepoMock.findOne(Mockito.anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setRedirectUri("http://acme.com/myApp");
		}});
		when(this.extSvcMock.getResponseTypes()).thenReturn(Arrays.asList("dummy1", "dummy2"));
		when(this.extSvcMock.getExtension(eq("dummy1"))).thenReturn(new IOAuthExtension() {
			public List<String> getAuthorizedScopes() { return new ArrayList<String>(); }
			public void token(NotesPrincipal user, Application app, Object context, List<String> askedScopes, IPropertyAdder adder) { }
			public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, List<String> responseTypes, IAuthorizer authorizer) {
				authorizer.addCodeToResponse(true);		// Will save the auth code
			}
		});
		when(this.extSvcMock.getExtension(eq("dummy2"))).thenReturn(new IOAuthExtension() {
			public List<String> getAuthorizedScopes() { return new ArrayList<String>(); }
			public void token(NotesPrincipal user, Application app, Object context, List<String> askedScopes, IPropertyAdder adder) { }
			public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, List<String> responseTypes, IAuthorizer authorizer) {
				authorizer.addCodeToResponse(false);		// Will NOT save the auth code
			}
		});
		MvcResult result = mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "dummy1 dummy2")
		)
		.andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		Map<String, String> params = urlParameters(location);
		assertThat(params, hasEntry("error", "server_error"));
		assertThat(params, hasEntry(equalTo("error_description"), containsString("response_type conflict on grant_type")));
	}
	
	/**
	 * Conflicting response types on properties
	 */
	@Test
	public void whenConflictingResponseTypeOnProps_thenError() throws Exception {
		when(appRepoMock.findOne(Mockito.anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setRedirectUri("http://acme.com/myApp");
		}});
		when(this.extSvcMock.getResponseTypes()).thenReturn(Arrays.asList("dummy1", "dummy2"));
		when(this.extSvcMock.getExtension(eq("dummy1"))).thenReturn(new IOAuthExtension() {
			public List<String> getAuthorizedScopes() { return new ArrayList<String>(); }
			public void token(NotesPrincipal user, Application app, Object context, List<String> askedScopes, IPropertyAdder adder) { }
			public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, List<String> responseTypes, IAuthorizer authorizer) {
				authorizer.addProperty("prop", "value");		// Adding property
			}
		});
		when(this.extSvcMock.getExtension(eq("dummy2"))).thenReturn(new IOAuthExtension() {
			public List<String> getAuthorizedScopes() { return new ArrayList<String>(); }
			public void token(NotesPrincipal user, Application app, Object context, List<String> askedScopes, IPropertyAdder adder) { }
			public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, List<String> responseTypes, IAuthorizer authorizer) {
				authorizer.addProperty("prop", "othervalue");		// Adding SAME property
			}
		});
		MvcResult result = mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "dummy1 dummy2")
		)
		.andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		Map<String, String> params = urlParameters(location);
		assertThat(params, hasEntry("error", "server_error"));
		assertThat(params, hasEntry(equalTo("error_description"), containsString("response_type conflict on properties")));
	}
	
	// =======================================================================
	
	/**
	 * Get an authorization code without a redirect uri.
	 * App only have one, so it's ok.
	 * https://tools.ietf.org/html/rfc6749#section-3.1
	 */
	@Test
	public void whenExtensionSavesAuthCode_thenSavedAuthCodeOK() throws Exception {
		when(appRepoMock.findOne(anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp" + appRoot);
			setRedirectUri("http://acme.com/myApp");
		}});
		when(this.extSvcMock.getResponseTypes()).thenReturn(Arrays.asList("dummy"));
		when(this.extSvcMock.getExtension(eq("dummy"))).thenReturn(new IOAuthExtension() {
			public List<String> getAuthorizedScopes() { return new ArrayList<String>(); }
			public void token(NotesPrincipal user, Application app, Object context, List<String> askedScopes, IPropertyAdder adder) { }
			public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, List<String> responseTypes, IAuthorizer authorizer) {
				authorizer.setContext(new DummyContext() {{
					setName("CN=Lionel/O=USER");
				}});
				authorizer.addCodeToResponse(true);
			}
		});
		
		when(authCodeRepoMock.save(any(AuthCodeEntity.class))).then(returnsFirstArg());
		
		MvcResult result = mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "dummy")
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
		
		assertThat(code.getFullName(), equalTo(user.getName()));
		assertThat(code.getCommonName(), equalTo(user.getCommon()));
		assertThat(code.getRedirectUri(), equalTo("http://acme.com/myApp"));
		assertThat(code.getClientId(), equalTo("1234"));
		assertThat(code.getExpires(), equalTo(TimeServiceTestImpl.CURRENT_TIME + authCodeLifetime));
		
		assertThat(code.getScopes(), containsInAnyOrder("azerty", "uiop"));
		assertThat(code.getGrantedScopes(), emptyIterable());
		
		assertThat(code.getContextClasses().size(), is(equalTo(1)));
		assertThat(code.getContextClasses(), hasKey("dummy"));
		
		String jsonCtx = code.getContextObjects().get("dummy");
		ObjectMapper mapper = new ObjectMapper();
		DummyContext ctx = mapper.readValue(jsonCtx, DummyContext.class);
		
		assertThat(ctx.getName(), equalTo("CN=Lionel/O=USER"));
	}
	
	/**
	 * Additional parameters in redirect uri.
	 * https://tools.ietf.org/html/rfc6749#section-3.1
	 */
	@Test
	public void whenExistingParamsInRedirectUri_thenParamStillInRedirection() throws Exception {
		when(appRepoMock.findOne(anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp" + appRoot);
			setRedirectUri("http://acme.com/myApp?param1=xxx");			// Existing parameters in uri
		}});
		when(this.extSvcMock.getResponseTypes()).thenReturn(Arrays.asList("dummy"));
		when(this.extSvcMock.getExtension(eq("dummy"))).thenReturn(new IOAuthExtension() {
			public List<String> getAuthorizedScopes() { return new ArrayList<String>(); }
			public void token(NotesPrincipal user, Application app, Object context, List<String> askedScopes, IPropertyAdder adder) { }
			public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, List<String> responseTypes, IAuthorizer authorizer) {
				authorizer.addProperty("dummy_authorize_param", "authparamvalue");
				authorizer.addCodeToResponse(true);
			}
		});
		
		when(authCodeRepoMock.save(any(AuthCodeEntity.class))).then(returnsFirstArg());
		
		MvcResult result = mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "dummy")
		)
		.andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		assertThat(location, startsWith("http://acme.com/myApp"));
		
		Map<String, String> params = urlParameters(location);
		assertThat(params, hasKey("code"));
		assertThat(params, hasEntry("param1", "xxx"));
		assertThat(params, hasEntry("dummy_authorize_param", "authparamvalue"));
	}
	
	/**
	 * Fragment in redirect_uri, and response_type = code
	 * https://tools.ietf.org/html/rfc6749#section-3.1
	 */
	@Test
	public void whenFragmentInRedirectUriAndSaveAuthCode_then500() throws Exception {
		when(appRepoMock.findOne(eq("1234"))).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp" + appRoot);
			setRedirectUri("http://acme.com/myApp#param1=xxx");			// Fragment in uri
		}});
		when(this.extSvcMock.getResponseTypes()).thenReturn(Arrays.asList("dummy"));
		when(this.extSvcMock.getExtension(eq("dummy"))).thenReturn(new IOAuthExtension() {
			public List<String> getAuthorizedScopes() { return new ArrayList<String>(); }
			public void token(NotesPrincipal user, Application app, Object context, List<String> askedScopes, IPropertyAdder adder) { }
			public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, List<String> responseTypes, IAuthorizer authorizer) {
				authorizer.addCodeToResponse(true);
			}
		});
		
		mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "dummy")
		)
		.andExpect(status().is(500))
		.andExpect(content().string(containsString("invalid redirect_uri")));
	}
	
	/**
	 * Not all scopes grantes
	 */
	@Test
	public void whenAllScopesNotGranted_thenGrantedScopesInAuthCode() throws Exception {
		when(appRepoMock.findOne(anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp" + appRoot);
			setRedirectUri("http://acme.com/myApp");
		}});
		when(this.extSvcMock.getResponseTypes()).thenReturn(Arrays.asList("dummy"));
		when(this.extSvcMock.getExtension(eq("dummy"))).thenReturn(new IOAuthExtension() {
			public List<String> getAuthorizedScopes() { return Arrays.asList("scope1", "scopeX"); }
			public void token(NotesPrincipal user, Application app, Object context, List<String> askedScopes, IPropertyAdder adder) { }
			public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, List<String> responseTypes, IAuthorizer authorizer) {
				authorizer.addCodeToResponse(true);
			}
		});
		
		when(authCodeRepoMock.save(any(AuthCodeEntity.class))).then(returnsFirstArg());
		
		mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "dummy")
				.param("scope", "scope1 scope2")
		)
		.andExpect(status().is(302));
		
		ArgumentCaptor<AuthCodeEntity> authCodeCaptor = ArgumentCaptor.forClass(AuthCodeEntity.class);
		Mockito.verify(authCodeRepoMock, Mockito.times(1)).save(authCodeCaptor.capture());
		List<AuthCodeEntity> added = authCodeCaptor.getAllValues();
		assertThat(added.size(), is(equalTo(1)));
		AuthCodeEntity code = added.get(0);
		
		assertThat(code.getScopes(), containsInAnyOrder("scope1", "scope2"));
		assertThat(code.getGrantedScopes(), containsInAnyOrder("scope1"));
	}
	
	/**
	 * Same scope granted by two extensions
	 */
	@Test
	public void whenSameScopeGrantedByTwoExtensions_thenScopesNotDoubled() throws Exception {
		when(appRepoMock.findOne(anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp" + appRoot);
			setRedirectUri("http://acme.com/myApp");
		}});
		when(this.extSvcMock.getResponseTypes()).thenReturn(Arrays.asList("dummy1", "dummy2"));
		when(this.extSvcMock.getExtension(eq("dummy1"))).thenReturn(new IOAuthExtension() {
			public List<String> getAuthorizedScopes() { return Arrays.asList("scope1", "scope2"); }
			public void token(NotesPrincipal user, Application app, Object context, List<String> askedScopes, IPropertyAdder adder) { }
			public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, List<String> responseTypes, IAuthorizer authorizer) {
				authorizer.addCodeToResponse(true);
			}
		});
		when(this.extSvcMock.getExtension(eq("dummy2"))).thenReturn(new IOAuthExtension() {
			public List<String> getAuthorizedScopes() { return Arrays.asList("scope1"); }
			public void token(NotesPrincipal user, Application app, Object context, List<String> askedScopes, IPropertyAdder adder) { }
			public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, List<String> responseTypes, IAuthorizer authorizer) {
			}
		});
		when(authCodeRepoMock.save(any(AuthCodeEntity.class))).then(returnsFirstArg());
		
		mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "dummy1 dummy2")		// Using dummy extension
				.param("scope", "scope1 scope2 scope3")
		)
		.andExpect(status().is(302));
		
		ArgumentCaptor<AuthCodeEntity> authCodeCaptor = ArgumentCaptor.forClass(AuthCodeEntity.class);
		Mockito.verify(authCodeRepoMock, Mockito.times(1)).save(authCodeCaptor.capture());
		List<AuthCodeEntity> added = authCodeCaptor.getAllValues();
		assertThat(added.size(), is(equalTo(1)));
		AuthCodeEntity code = added.get(0);
		
		assertThat(code.getScopes(), containsInAnyOrder("scope1", "scope2", "scope3"));
		assertThat(code.getGrantedScopes().size(), equalTo(2));
		assertThat(code.getGrantedScopes(), containsInAnyOrder("scope1", "scope2"));
	}
	
	// ======================================================================================
	
	/**
	 * Flow without grant (not saving auth code)
	 */
	@Test
	public void whenAuthCodeNotSaved_thenNoCodeInRedirection() throws Exception {
		when(appRepoMock.findOne(anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp" + appRoot);
			setRedirectUri("http://acme.com/myApp");
		}});
		when(this.extSvcMock.getResponseTypes()).thenReturn(Arrays.asList("dummy"));
		when(this.extSvcMock.getExtension(eq("dummy"))).thenReturn(new IOAuthExtension() {
			public List<String> getAuthorizedScopes() { return Arrays.asList(); }
			public void token(NotesPrincipal user, Application app, Object context, List<String> askedScopes, IPropertyAdder adder) { }
			public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, List<String> responseTypes, IAuthorizer authorizer) {
				authorizer.addCodeToResponse(false);
			}
		});
		
		when(authCodeRepoMock.save(any(AuthCodeEntity.class))).then(returnsFirstArg());
				
		MvcResult result = mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "dummy")
				.param("state", "myState")
		)
		.andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		assertThat(location, startsWith("http://acme.com/myApp"));
		
		Map<String, String> params = urlRefs(location);
		assertThat(params, hasEntry("state", "myState"));
		assertThat(params, not(hasKey("code")));
	}
	
	/**
	 * Additional parameters in redirect uri
	 * WARNING: RFC says that fragments are not allowed. 
	 * But our implementation allowed them when using "token" response type, because token is already sent in a fragment.
	 * https://tools.ietf.org/html/rfc6749#section-3.1
	 */
	@Test
	public void whenFragmentInRedirectUriAndAuthCodeNotSaved_thenOK() throws Exception {
		when(appRepoMock.findOne(anyString())).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp" + appRoot);
			setRedirectUri("http://acme.com/myApp#param1=xxx");			// Existing parameters in uri
		}});
		when(this.extSvcMock.getResponseTypes()).thenReturn(Arrays.asList("dummy"));
		when(this.extSvcMock.getExtension(eq("dummy"))).thenReturn(new IOAuthExtension() {
			public List<String> getAuthorizedScopes() { return Arrays.asList(); }
			public void token(NotesPrincipal user, Application app, Object context, List<String> askedScopes, IPropertyAdder adder) { }
			public void authorize(NotesPrincipal user, Application app, List<String> askedScopes, List<String> responseTypes, IAuthorizer authorizer) {
				authorizer.addProperty("test", "testvalue");
				authorizer.addCodeToResponse(false);
			}
		});
		
		when(authCodeRepoMock.save(any(AuthCodeEntity.class))).then(returnsFirstArg());
		
		MvcResult result = mockMvc
		.perform(
				get("/authorize")
				.param("client_id", "1234")
				.param("response_type", "dummy")
		)
		.andExpect(status().is(302))
		.andReturn();
		
		String location = result.getResponse().getHeader("Location");
		assertThat(location, startsWith("http://acme.com/myApp"));
		
		Map<String, String> params = urlRefs(location);
		assertThat(params, hasEntry("param1", "xxx"));
		assertThat(params, hasEntry("test", "testvalue"));
		assertThat(params, not(hasKey("code")));
	}
}
