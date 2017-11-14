package com.github.lhervier.domino.oauth.server.testsuite.service;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.entity.PersonEntity;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.repo.ApplicationRepository;
import com.github.lhervier.domino.oauth.server.repo.PersonRepository;
import com.github.lhervier.domino.oauth.server.services.AppService;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;

public class TestAppService extends BaseTest {

	@Autowired
	private ApplicationRepository appRepoMock;
	
	@Autowired
	private PersonRepository personRepoMock;
	
	@Autowired
	private AppService appSvc;
	
	@Before
	public void setUp() {
		reset(appRepoMock);
	}
	
	@Test
	public void getApplicationNames() throws Exception {
		when(appRepoMock.listNames()).thenReturn(Arrays.asList("app1", "app2"));
		
		assertThat(
				appSvc.getApplicationsNames(), 
				containsInAnyOrder("app1", "app2")
		);
	}
	
	@Test
	public void getApplicationFromName() throws Exception {
		// Expected behavior of repository
		when(
				appRepoMock
				.findOneByName(eq("testApp"))
		).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("testApp");
			setFullName("CN=testApp/O=APPLICATION");		// Different from value configured in test.properties !
			setRedirectUri("http://acme.com/testApp");
			setRedirectUris(Arrays.asList("http://acm.com/testApp/login", "http://acm.com/testApp/login2"));
			setReaders("*");
			setAppReader("dummy value");
		}});
		
		// Run test
		Application app = appSvc.getApplicationFromName("testApp");
		
		// Check repo has been called accordingly
		verify(appRepoMock, times(1)).findOneByName("testApp");
		verifyNoMoreInteractions(appRepoMock);
		
		// Check result is correst
		assertThat(app.getFullName(), is(equalTo("CN=testApp/O=APPLICATION")));		// Must not change
		assertThat(app.getClientId(), is(equalTo("1234")));
		assertThat(app.getName(), is(equalTo("testApp")));
		assertThat(app.getReaders(), is(equalTo("*")));
		assertThat(app.getRedirectUri(), is(equalTo("http://acme.com/testApp")));
		assertThat(app.getRedirectUris(), allOf(
				notNullValue(), 
				containsInAnyOrder("http://acm.com/testApp/login", "http://acm.com/testApp/login2"))
		);
	}
	
	@Test
	public void getApplicationFromClientId() throws Exception {
		when(
				appRepoMock
				.findOne(eq("1234"))
		).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("testApp");
			setFullName("CN=testApp/O=APPLICATION");
			setRedirectUri("http://acme.com/testApp");
			setRedirectUris(Arrays.asList("http://acm.com/testApp/login", "http://acm.com/testApp/login2"));
			setReaders("*");
			setAppReader("dummy value");
		}});
		
		Application app = appSvc.getApplicationFromClientId("1234");
		
		verify(appRepoMock, times(1)).findOne("1234");
		verifyNoMoreInteractions(appRepoMock);
		
		assertThat(app.getFullName(), is(equalTo("CN=testApp/O=APPLICATION")));
		assertThat(app.getClientId(), is(equalTo("1234")));
		assertThat(app.getName(), is(equalTo("testApp")));
		assertThat(app.getReaders(), is(equalTo("*")));
		assertThat(app.getRedirectUri(), is(equalTo("http://acme.com/testApp")));
		assertThat(app.getRedirectUris(), allOf(
				notNullValue(), 
				containsInAnyOrder("http://acm.com/testApp/login", "http://acm.com/testApp/login2"))
		);
	}
	
	@Test
	public void differentIdsGenerated() throws Exception {
		Application app1 = appSvc.prepareApplication();
		verifyNoMoreInteractions(appRepoMock);
		
		Application app2 = appSvc.prepareApplication();
		verifyNoMoreInteractions(appRepoMock);
		
		assertThat(app1.getClientId(), not(equalTo(app2.getClientId())));
	}
	
	@Test
	public void computeFullNameOnSave() throws Exception {
		when(personRepoMock.save(any(PersonEntity.class))).thenReturn(new PersonEntity() {{
			setHttpPassword("password");
		}});
		
		appSvc.addApplication(new Application() {{
			setClientId("123456");
			setName("app1");
			setReaders("*");
			setRedirectUri("http://acme.com/app1");
			setRedirectUris(new ArrayList<String>());
		}});
		
		verify(appRepoMock, times(1)).save(any(ApplicationEntity.class));
		
		ArgumentCaptor<PersonEntity> personCaptor = ArgumentCaptor.forClass(PersonEntity.class);
		verify(personRepoMock, times(1)).save(personCaptor.capture());
		
		List<PersonEntity> added = personCaptor.getAllValues();
		assertThat(added.size(), is(equalTo(1)));
		assertThat(added.get(0).getFullNames(), containsInAnyOrder("CN=app1/OU=APPLICATION/O=WEB", "123456"));
	}
	
	@Test(expected = DataIntegrityViolationException.class)
	public void saveWithExistingName() throws Exception {
		when(appRepoMock.findOneByName(eq("myApp"))).thenReturn(new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setFullName("CN=myApp/OU=APPLICATION/O=WEB");
		}});
		
		appSvc.addApplication(new Application() {{
			setClientId("123456");
			setName("myApp");
			setReaders("*");
			setRedirectUri("http://acme.com/app1");
			setRedirectUris(new ArrayList<String>());
		}});
	}
	
	@Test(expected = DataIntegrityViolationException.class)
	public void saveWithExistingClientId() throws Exception {
		when(appRepoMock.findOne(eq("123456"))).thenReturn(new ApplicationEntity() {{
			setClientId("123456");
			setName("myOtherExistingApp");
			setFullName("CN=myApp/OU=APPLICATION/O=WEB");
		}});
		
		appSvc.addApplication(new Application() {{
			setClientId("123456");
			setName("myApp");
			setReaders("*");
			setRedirectUri("http://acme.com/app1");
			setRedirectUris(new ArrayList<String>());
		}});
	}
	
	@Test
	public void updateApplication() throws Exception {
		ApplicationEntity entity = new ApplicationEntity() {{
			setClientId("1234");
			setName("myApp");
			setReaders("XX");
			setRedirectUri("http://acme.com/olduri");
			setRedirectUris(new ArrayList<String>());
		}};
		when(appRepoMock.findOne(eq("1234"))).thenReturn(entity);
		when(appRepoMock.findOneByName(eq("myApp"))).thenReturn(entity);
		
		appSvc.updateApplication(new Application() {{
			setClientId("1234");
			setName("myApp");
			setReaders("*");
			setRedirectUri("http://acme.com/myapp");
			setRedirectUris(new ArrayList<String>());
			getRedirectUris().add("http://acme.com/myapp2");
		}});
		
		ArgumentCaptor<ApplicationEntity> appCaptor = ArgumentCaptor.forClass(ApplicationEntity.class);
		verify(appRepoMock, times(1)).save(appCaptor.capture());
		
	}
	
	@Test
	public void testUpdateWithInvalidRedirectUri() throws Exception {
		
	}
	
	@Test
	public void testUpdateWithOneInvalidRedirectUri() throws Exception {
		
	}
}
