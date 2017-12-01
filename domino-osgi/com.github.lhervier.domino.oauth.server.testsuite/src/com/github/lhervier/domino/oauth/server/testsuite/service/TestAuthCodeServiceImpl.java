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
import com.github.lhervier.domino.oauth.server.ext.core.CoreContext;
import com.github.lhervier.domino.oauth.server.ext.core.CoreExt;
import com.github.lhervier.domino.oauth.server.services.impl.AuthCodeServiceImpl;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.impl.TimeServiceTestImpl;

@SuppressWarnings("serial")
public class TestAuthCodeServiceImpl extends BaseTest {

	/**
	 * The auth code service
	 */
	@Autowired
	private AuthCodeServiceImpl authCodeSvc;
	
	/**
	 * The core context
	 */
	@Autowired
	private CoreExt coreExt;
	
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
		assertThat(this.authCodeSvc.toEntity("invalid_refresh_token"), is(nullValue()));
	}
	
	/**
	 * Expired JWE
	 */
	@Test
	public void expiredJwe() throws Exception {
		// Create an entity
		AuthCodeEntity entity = new AuthCodeEntity() {{
			setApplication("myApp");
			setClientId("1234");
			setExpires(timeSvcStub.currentTimeSeconds() + refreshTokenLifetime);
			setContextClasses(new HashMap<String, String>() {{
				put(coreExt.getId(), CoreContext.class.getName());
			}});
			setContextObjects(new HashMap<String, String>() {{
				put(coreExt.getId(), mapper.writeValueAsString(new CoreContext() {{
					setAud("1234");
					setIss(coreIss);
					setSub("CN=Lionel/O=USER");
				}}));
			}});
		}};
		String refreshToken = this.authCodeSvc.fromEntity(entity);
		
		// Time pass...
		TimeServiceTestImpl.CURRENT_TIME += this.refreshTokenLifetime + 10L;
		
		// refresh token no longer valid
		AuthCodeEntity authCode = this.authCodeSvc.toEntity(refreshToken);
		assertThat(authCode, is(nullValue()));
	}
	
	/**
	 * Normal jwe
	 */
	@Test
	public void normalJwe() throws Exception {
		// Create an entity
		AuthCodeEntity entity = new AuthCodeEntity() {{
			setId("0123456789");
			setApplication("myApp");
			setClientId("1234");
			setRedirectUri("http://acme.com/myApp");
			setExpires(timeSvcStub.currentTimeSeconds() + refreshTokenLifetime);
			setContextClasses(new HashMap<String, String>() {{
				put(coreExt.getId(), CoreContext.class.getName());
			}});
			setContextObjects(new HashMap<String, String>() {{
				put(coreExt.getId(), mapper.writeValueAsString(new CoreContext() {{
					setAud("1234");
					setIss(coreIss);
					setSub("CN=Lionel/O=USER");
				}}));
			}});
		}};
		String refreshToken = this.authCodeSvc.fromEntity(entity);
		
		// Read the token back again
		AuthCodeEntity authCode = this.authCodeSvc.toEntity(refreshToken);
		assertThat(authCode.getId(), equalTo("0123456789"));
		assertThat(authCode.getApplication(), equalTo("myApp"));
		assertThat(authCode.getClientId(), equalTo("1234"));
		assertThat(authCode.getRedirectUri(), equalTo("http://acme.com/myApp"));
		assertThat(authCode.getExpires(), equalTo(timeSvcStub.currentTimeSeconds() + refreshTokenLifetime));
		
		assertThat(authCode.getContextClasses(), hasEntry(this.coreExt.getId(), CoreContext.class.getName()));
		assertThat(authCode.getContextClasses().size(), is(1));
		
		assertThat(authCode.getContextObjects().size(), is(1));
		assertThat(authCode.getContextObjects(), IsMapContaining.hasKey(this.coreExt.getId()));
		
		String json = authCode.getContextObjects().get(this.coreExt.getId());
		CoreContext ctx = this.mapper.readValue(json, CoreContext.class);
		
		assertThat(ctx.getAud(), equalTo("1234"));
		assertThat(ctx.getSub(), equalTo("CN=Lionel/O=USER"));
		assertThat(ctx.getIss(), equalTo(this.coreIss));
	}
}
