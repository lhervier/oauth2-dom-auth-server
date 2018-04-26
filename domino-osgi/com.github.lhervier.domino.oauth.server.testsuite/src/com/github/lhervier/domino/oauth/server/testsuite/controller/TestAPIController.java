package com.github.lhervier.domino.oauth.server.testsuite.controller;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.github.lhervier.domino.oauth.server.NotesPrincipal.AuthType;
import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.entity.PersonEntity;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.model.ClientType;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.impl.NotesPrincipalTestImpl;

public class TestAPIController extends BaseTest {

	@Autowired
	private NotesPrincipalTestImpl user;
	
	@Before
	public void setUp() {
		user.setAuthType(AuthType.NOTES);
		user.setCommon("Administrator");
		user.setCurrentDatabasePath(this.oauth2Db);
		user.setName("CN=Administrator/O=USERS");
		user.getRoles().add("[AppsManager]");
	}
	
	@Test
	public void whenUserDoesNotHaveRightRole_thenError() throws Exception {
		user.getRoles().clear();
		this.mockMvc.perform(
				get("/api/applications")
		).andExpect(status().is(403));
	}
	
	// ===================================================================================================================
	
	@Test
	public void whenListApplications_thenReturnApplications() throws Exception {
		when(appRepoMock.listNames()).thenReturn(Arrays.asList("app1", "app2"));
		when(appRepoMock.findOneByName(Mockito.eq("app1"))).thenReturn(new ApplicationEntity() {{
			setName("app1");
			setFullName("CN=app1/O=APP");
			setClientId("clientId1");
			setClientType(ClientType.CONFIDENTIAL.name());
			setReaders("*");
			setRedirectUri("http://acme.com/app1");
			setRedirectUris(Arrays.asList("http://acme.com/app1/login", "http://acme.com/app1/init"));
		}});
		when(appRepoMock.findOneByName(Mockito.eq("app2"))).thenReturn(new ApplicationEntity() {{
			setName("app2");
			setFullName("CN=app2/O=APP");
			setClientId("clientId2");
			setClientType(ClientType.PUBLIC.name());
			setReaders("*");
			setRedirectUri("http://acme.com/app2");
			setRedirectUris(Arrays.asList("http://acme.com/app2/login", "http://acme.com/app2/init"));
		}});
		
		MvcResult result = this.mockMvc.perform(
				get("/api/applications")
		).andExpect(status().is(200))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		Map<String, Object>[] resp = this.arrayFromJson(json);
		assertThat(resp.length, is(2));
		
		Map<String, Object> app1 = resp[0];
		assertThat((String) app1.get("name"), is("app1"));
		assertThat((String) app1.get("clientId"), is("clientId1"));
		
		Map<String, Object> app2 = resp[1];
		assertThat((String) app2.get("name"), is("app2"));
		assertThat((String) app2.get("clientId"), is("clientId2"));
	}
	
	// ===================================================================================================================
	
	@Test
	public void whenGetNonExistingAppDetails_then404() throws Exception {
		this.mockMvc.perform(
				get("/api/applications/non-existing-client-id")
		).andExpect(status().is(404))
		.andReturn();
	}
	
	@Test
	public void whenGetExistingAppDetails_thenOK() throws Exception {
		when(appRepoMock.findOne(Mockito.eq("clientId1"))).thenReturn(new ApplicationEntity() {{
			setName("app1");
			setFullName("CN=app1/O=APP");
			setClientId("clientId1");
			setClientType(ClientType.CONFIDENTIAL.name());
			setReaders("*");
			setRedirectUri("http://acme.com/app1");
			setRedirectUris(Arrays.asList("http://acme.com/app1/login", "http://acme.com/app1/init"));
		}});
		MvcResult result = this.mockMvc.perform(
				get("/api/applications/clientId1")
		).andExpect(status().is(200))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		Map<String, Object> app = this.fromJson(json);
		
		assertThat((String) app.get("name"), is("app1"));
		assertThat((String) app.get("clientId"), is("clientId1"));
		assertThat((String) app.get("fullName"), is("CN=app1/O=APP"));
		assertThat((String) app.get("clientType"), is("CONFIDENTIAL"));
		assertThat((String) app.get("redirectUri"), is("http://acme.com/app1"));
		assertThat((String) app.get("readers"), is("*"));
		
		@SuppressWarnings("unchecked")
		List<Object> redirectUris = (List<Object>) app.get("redirectUris");
		assertThat(redirectUris.size(), is(2));
		assertThat((String) redirectUris.get(0), is("http://acme.com/app1/login"));
		assertThat((String) redirectUris.get(1), is("http://acme.com/app1/init"));
	}
	
	@Test
	public void whenApplicationStoredWithoutClientType_thenClientTypeIsPublic() throws Exception {
		when(appRepoMock.findOne(Mockito.eq("clientId1"))).thenReturn(new ApplicationEntity() {{
			setName("app1");
			setFullName("CN=app1/O=APP");
			setClientId("clientId1");
			// setClientType(ClientType.CONFIDENTIAL.name());		// No client Type in database !
			setReaders("*");
			setRedirectUri("http://acme.com/app1");
			setRedirectUris(Arrays.asList("http://acme.com/app1/login", "http://acme.com/app1/init"));
		}});
		MvcResult result = this.mockMvc.perform(
				get("/api/applications/clientId1")
		).andExpect(status().is(200))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		Map<String, Object> app = this.fromJson(json);
		assertThat((String) app.get("clientType"), is("PUBLIC"));
	}
	
	// ===================================================================================================================
	
	@Test
	public void whenSaveNewApplicationWithExistingName_thenError() throws Exception {
		when(appRepoMock.findOneByName(Mockito.eq("app1"))).thenReturn(new ApplicationEntity() {{
			setClientId("clientId1");
			setName("app1");
			setFullName("CN=app1/O=APP");
			setRedirectUri("http://acme.com");
			setReaders("*");
		}});
		
		Application app = new Application();
		app.setClientType(ClientType.CONFIDENTIAL);
		app.setName("app1");
		app.setReaders("*");
		app.setRedirectUri("http://other;acme.com");
		this.mockMvc.perform(
				post("/api/applications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.mapper.writeValueAsString(app))
		).andExpect(status().is(400));
		
		verify(appRepoMock, times(0)).save(Mockito.any(ApplicationEntity.class));
		verify(personRepoMock, times(0)).save(Mockito.any(PersonEntity.class));
	}
	
	@Test
	public void whenSaveNewApplication_thenGetClientIdAndSecret() throws Exception {
		when(appRepoMock.save(Mockito.any(ApplicationEntity.class))).thenReturn(null);		// Return value not used
		when(personRepoMock.save(Mockito.any(PersonEntity.class))).thenReturn(new PersonEntity() {{
			setHttpPassword("password");
		}});
		
		Application app = new Application();
		app.setClientType(ClientType.CONFIDENTIAL);
		app.setName("app1");
		app.setReaders("*");
		app.setRedirectUri("http://acme.com");
		app.setRedirectUris(Arrays.asList("http://acme.com/login", "http://acme.com/init"));
		MvcResult result = this.mockMvc.perform(
				post("/api/applications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.mapper.writeValueAsString(app))
		).andExpect(status().is(200))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		Map<String, Object> resp = this.fromJson(json);
		
		assertThat((String) resp.get("clientId"), notNullValue());
		assertThat((String) resp.get("secret"), is("password"));
	}
	
	@Test
	public void whenSaveNewApplicationWithoutClientType_thenClientTypeIsPublic() throws Exception {
		final List<ApplicationEntity> savedEntities = new ArrayList<ApplicationEntity>();
		when(appRepoMock.save(Mockito.any(ApplicationEntity.class))).thenAnswer(new Answer<ApplicationEntity>() {
			@Override
			public ApplicationEntity answer(InvocationOnMock invocation) throws Throwable {
				savedEntities.add(invocation.getArgumentAt(0, ApplicationEntity.class));
				return null;	// Return value not used
			}
		});
		when(personRepoMock.save(Mockito.any(PersonEntity.class))).thenReturn(new PersonEntity() {{
			setHttpPassword("password");
		}});
		
		Application app = new Application();
		app.setClientType(null);		// No client type
		app.setName("app1");
		app.setReaders("*");
		app.setRedirectUri("http://acme.com");
		this.mockMvc.perform(
				post("/api/applications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.mapper.writeValueAsString(app))
		).andExpect(status().is(200));
		
		verify(this.appRepoMock, times(1)).save(Mockito.any(ApplicationEntity.class));
		assertThat(savedEntities.size(), is(1));
		assertThat(savedEntities.get(0).getClientType(), is(ClientType.PUBLIC.name()));
	}
	
	@Test
	public void whenSaveNewApplicationWithInvalidRedirectUri_thenError() throws Exception {
		Application app = new Application();
		app.setClientType(ClientType.CONFIDENTIAL);
		app.setName("app1");
		app.setReaders("*");
		app.setRedirectUri("invaliduri");
		this.mockMvc.perform(
				post("/api/applications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.mapper.writeValueAsString(app))
		).andExpect(status().is(400));
		
		verify(appRepoMock, times(0)).save(Mockito.any(ApplicationEntity.class));
		verify(personRepoMock, times(0)).save(Mockito.any(PersonEntity.class));
	}
	
	@Test
	public void whenSaveNewApplicationWithInvalidAdditionnalRedirectUri_thenError() throws Exception {
		Application app = new Application();
		app.setClientType(ClientType.CONFIDENTIAL);
		app.setName("app1");
		app.setReaders("*");
		app.setRedirectUri("http://acme.com");
		app.setRedirectUris(Arrays.asList("invaliduri"));
		this.mockMvc.perform(
				post("/api/applications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.mapper.writeValueAsString(app))
		).andExpect(status().is(400));
		
		verify(appRepoMock, times(0)).save(Mockito.any(ApplicationEntity.class));
		verify(personRepoMock, times(0)).save(Mockito.any(PersonEntity.class));
	}
	
	// ===================================================================================================================
	
	@Test
	public void whenUpdateApplicationName_thenError() throws Exception {
		when(appRepoMock.findOne(Mockito.eq("clientId1"))).thenReturn(new ApplicationEntity() {{
			setClientId("clientId1");
			setName("app1");
			setFullName("CN=app1/O=APP");
			setClientType(ClientType.CONFIDENTIAL.name());
			setRedirectUri("http://acme.com");
		}});
		when(appRepoMock.save(Mockito.any(ApplicationEntity.class))).thenReturn(null);		// Return value not used
		
		Application app = new Application();
		app.setClientId("clientId1");
		app.setClientType(ClientType.CONFIDENTIAL);
		app.setName("NewNameOfMyApp");		// Forbidden !
		app.setReaders("*");
		app.setRedirectUri("http://acme.com");
		MvcResult result = this.mockMvc.perform(
				post("/api/applications/clientId1/put")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.mapper.writeValueAsString(app))
		).andExpect(status().is(400))
		.andReturn();
		
		String json = result.getResponse().getContentAsString();
		Map<String, Object> resp = this.fromJson(json);
		assertThat((String) resp.get("error"), is("Cannot change name of application..."));
	}
	
	@Test
	public void whenUpdateUnknownApp_thenError() throws Exception {
		Application app = new Application();
		app.setClientId("clientId1");
		app.setClientType(ClientType.CONFIDENTIAL);
		app.setName("app1");
		app.setReaders("*");
		app.setRedirectUri("http://acme.com");
		this.mockMvc.perform(
				post("/api/applications/clientId1/put")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.mapper.writeValueAsString(app))
		).andExpect(status().is(400));
		
		verify(this.appRepoMock, times(1)).findOne(Mockito.eq("clientId1"));
		verify(this.appRepoMock, times(0)).save(Mockito.any(ApplicationEntity.class));
	}
	
	@Test
	public void whenUpdateApplication_thenOK() throws Exception {
		when(appRepoMock.findOne(Mockito.eq("clientId1"))).thenReturn(new ApplicationEntity() {{
			setClientId("clientId1");
			setName("app1");
			setFullName("CN=app1/O=APP");
			setClientType(ClientType.CONFIDENTIAL.name());
			setRedirectUri("http://acme.com");
		}});
		when(appRepoMock.save(Mockito.any(ApplicationEntity.class))).thenReturn(null);		// Return value not used
		
		Application app = new Application();
		app.setClientId("clientId1");
		app.setName("app1");
		app.setClientType(ClientType.CONFIDENTIAL);
		app.setReaders("*");
		app.setRedirectUri("http://acme.com/login");
		this.mockMvc.perform(
				post("/api/applications/clientId1/put")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.mapper.writeValueAsString(app))
		).andExpect(status().is(200));
		
		verify(this.appRepoMock, times(1)).save(Mockito.any(ApplicationEntity.class));
	}
	
	@Test
	public void whenDeleteNonExistingApplication_thenError() throws Exception {
		this.mockMvc.perform(
				get("/api/applications/nonExistingClientId/delete")
		).andExpect(status().is(400));
		verify(this.appRepoMock, times(1)).findOne(Mockito.eq("nonExistingClientId"));
		verify(this.appRepoMock, times(0)).deleteByName(Mockito.any(String.class));
	}
	
	@Test
	public void whenDeleteExistingApplication_thenOK() throws Exception {
		when(appRepoMock.findOne(Mockito.eq("clientId1"))).thenReturn(new ApplicationEntity() {{
			setClientId("clientId1");
			setName("app1");
			setFullName("CN=app1/O=APP");
			setClientType(ClientType.CONFIDENTIAL.name());
			setRedirectUri("http://acme.com");
		}});
		
		Application app = new Application();
		app.setClientId("clientId1");
		app.setName("app1");
		app.setClientType(ClientType.CONFIDENTIAL);
		app.setReaders("*");
		app.setRedirectUri("http://acme.com/login");
		this.mockMvc.perform(
				get("/api/applications/clientId1/delete")
		).andExpect(status().is(200));
		
		verify(this.appRepoMock, times(1)).deleteByName(Mockito.eq("app1"));
	}
}
