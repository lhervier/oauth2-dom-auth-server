package com.github.lhervier.domino.oauth.server.testsuite.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import org.hamcrest.collection.IsMapContaining;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.services.impl.JWTServiceImpl;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.impl.DummyContext;
import com.github.lhervier.domino.oauth.server.testsuite.impl.TimeServiceTestImpl;

@SuppressWarnings("serial")
public class TestJWTServiceImpl extends BaseTest {

	/**
	 * The jwt service
	 */
	@Autowired
	private JWTServiceImpl jwtSvc;
	
	/**
	 * Before execution of each test
	 */
	@Before
	public void before() throws Exception {
		
	}
	
	/**
	 * Invalid jwe
	 */
	@Test
	public void invalidJwe() throws Exception {
		assertThat(this.jwtSvc.fromJwe("invalid_refresh_token", "xx", AuthCodeEntity.class), is(nullValue()));
	}
	
	/**
	 * Expired JWE
	 */
	@Test
	public void whenExpiredAuthCode_thenNull() throws Exception {
		// Create an entity
		AuthCodeEntity entity = new AuthCodeEntity() {{
			setApplication("myApp");
			setClientId("1234");
			setExpires(timeSvcStub.currentTimeSeconds() + refreshTokenLifetime);
			setContextClasses(new HashMap<String, String>() {{
				put("dummy", DummyContext.class.getName());
			}});
			setContextObjects(new HashMap<String, String>() {{
				put("dummy", mapper.writeValueAsString(new DummyContext() {{
					setName("CN=Lionel/O=USER");
				}}));
			}});
		}};
		String refreshToken = this.jwtSvc.createJwe(entity, "xx");
		
		// Time pass...
		TimeServiceTestImpl.CURRENT_TIME += this.refreshTokenLifetime + 10L;
		
		// refresh token no longer valid
		AuthCodeEntity authCode = this.jwtSvc.fromJwe(refreshToken, "xx", AuthCodeEntity.class);
		assertThat(authCode, is(nullValue()));
	}
	
	/**
	 * Normal jwe
	 */
	@Test
	public void whenNormalAuthCode_thenOK() throws Exception {
		// Create an entity
		AuthCodeEntity entity = new AuthCodeEntity() {{
			setId("0123456789");
			setApplication("myApp");
			setClientId("1234");
			setRedirectUri("http://acme.com/myApp");
			setExpires(timeSvcStub.currentTimeSeconds() + refreshTokenLifetime);
			setContextClasses(new HashMap<String, String>() {{
				put("dummy", DummyContext.class.getName());
			}});
			setContextObjects(new HashMap<String, String>() {{
				put("dummy", mapper.writeValueAsString(new DummyContext() {{
					setName("CN=Lionel/O=USER");
				}}));
			}});
		}};
		String refreshToken = this.jwtSvc.createJwe(entity, "xx");
		
		// Read the token back again
		AuthCodeEntity authCode = this.jwtSvc.fromJwe(refreshToken, "xx", AuthCodeEntity.class);
		assertThat(authCode.getId(), equalTo("0123456789"));
		assertThat(authCode.getApplication(), equalTo("myApp"));
		assertThat(authCode.getClientId(), equalTo("1234"));
		assertThat(authCode.getRedirectUri(), equalTo("http://acme.com/myApp"));
		assertThat(authCode.getExpires(), equalTo(timeSvcStub.currentTimeSeconds() + refreshTokenLifetime));
		
		assertThat(authCode.getContextClasses(), hasEntry("dummy", DummyContext.class.getName()));
		assertThat(authCode.getContextClasses().size(), is(1));
		
		assertThat(authCode.getContextObjects().size(), is(1));
		assertThat(authCode.getContextObjects(), IsMapContaining.hasKey("dummy"));
		
		String json = authCode.getContextObjects().get("dummy");
		DummyContext ctx = this.mapper.readValue(json, DummyContext.class);
		
		assertThat(ctx.getName(), equalTo("CN=Lionel/O=USER"));
	}
}
