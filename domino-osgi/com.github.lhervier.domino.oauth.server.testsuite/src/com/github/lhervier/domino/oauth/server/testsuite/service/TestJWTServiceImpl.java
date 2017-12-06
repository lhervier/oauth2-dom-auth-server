package com.github.lhervier.domino.oauth.server.testsuite.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import org.hamcrest.collection.IsMapContaining;
import org.junit.Test;

import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ext.core.AccessToken;
import com.github.lhervier.domino.oauth.server.testsuite.BaseTest;
import com.github.lhervier.domino.oauth.server.testsuite.impl.DummyContext;
import com.github.lhervier.domino.oauth.server.testsuite.impl.SecretRepositoryTestImpl;
import com.github.lhervier.domino.oauth.server.testsuite.impl.TimeServiceTestImpl;

@SuppressWarnings("serial")
public class TestJWTServiceImpl extends BaseTest {

	/**
	 * Invalid jwe
	 */
	@Test
	public void whenInvalidJwe_thenObjectIsNull() throws Exception {
		assertThat(this.jwtSvc.fromJwe("invalid_refresh_token", "xx", AuthCodeEntity.class), is(nullValue()));
	}
	
	/**
	 * Invalid JWS
	 */
	@Test
	public void whenInvalidJws_thenObjectIsNull() throws Exception {
		assertThat(this.jwtSvc.fromJwe("invalid_access_token", "xx", AccessToken.class), is(nullValue()));
	}
	
	/**
	 * Expired JWE
	 */
	@Test
	public void whenExpiredJwe_thenObjectIsNull() throws Exception {
		// Create an entity
		AuthCodeEntity entity = new AuthCodeEntity() {{
			setApplication("myApp");
			setClientId("1234");
			setExpires(timeSvc.currentTimeSeconds() + refreshTokenLifetime);
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
	 * Expired JWS
	 */
	@Test
	public void whenExpiredJws_thenObjectIsNull() throws Exception {
		// Create an entity
		AccessToken accessToken = new AccessToken() {{
			setAud("1234");
			setIss(coreIss);
			setSub("CN=Lionel/O=USER");
			setExpires(timeSvc.currentTimeSeconds() + refreshTokenLifetime);
		}};
		String jws = this.jwtSvc.createJws(accessToken, "xx");
		
		// Time pass...
		TimeServiceTestImpl.CURRENT_TIME += this.refreshTokenLifetime + 10L;
		
		// refresh token no longer valid
		assertThat(this.jwtSvc.fromJws(jws, "xx", AccessToken.class), is(nullValue()));
	}
	
	/**
	 * Jws encoded with wrong key
	 */
	@Test
	public void whenJwsSignedWithWrongKey_thenObjectIdNull() throws Exception {
		// Create an access token
		AccessToken accessToken = new AccessToken() {{
			setAud("1234");
			setIss(coreIss);
			setSub("CN=Lionel/O=USER");
			setExpires(timeSvc.currentTimeSeconds() + 10L);
		}};
		String jws = this.jwtSvc.createJws(accessToken, "KEY1");		// Signed using KEY1
		
		// Read the token back again
		AccessToken accessTokenDeser = this.jwtSvc.fromJws(jws, "KEY2", AccessToken.class);
		assertThat(accessTokenDeser, is(nullValue()));
	}
	
	/**
	 * Jws signed with the bad key
	 */
	@Test
	public void whenJwsSignedWithBadKey_thenObjectIsNull() throws Exception {
		// Create an access token
		AccessToken accessToken = new AccessToken() {{
			setAud("1234");
			setIss(coreIss);
			setSub("CN=Lionel/O=USER");
			setExpires(timeSvc.currentTimeSeconds() + 10L);
		}};
		String jws = this.jwtSvc.createJws(accessToken, "KEY1");		// Signed using KEY1
		
		// Change sign key
		SecretRepositoryTestImpl.SIGN_KEY = "AZERTYUIOPQSDFGHJKLMWXCVBNAZERTY";
		
		// Try to validate the ticket
		AccessToken bad = this.jwtSvc.fromJws(jws, "KEY1", AccessToken.class);		// Same key
		assertThat(bad, is(nullValue()));
	}
	
	/**
	 * Normal jwe
	 */
	@Test
	public void whenNormalJwe_thenOK() throws Exception {
		// Create an entity
		AuthCodeEntity entity = new AuthCodeEntity() {{
			setId("0123456789");
			setApplication("myApp");
			setClientId("1234");
			setRedirectUri("http://acme.com/myApp");
			setExpires(timeSvc.currentTimeSeconds() + refreshTokenLifetime);
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
		assertThat(authCode.getExpires(), equalTo(timeSvc.currentTimeSeconds() + refreshTokenLifetime));
		
		assertThat(authCode.getContextClasses(), hasEntry("dummy", DummyContext.class.getName()));
		assertThat(authCode.getContextClasses().size(), is(1));
		
		assertThat(authCode.getContextObjects().size(), is(1));
		assertThat(authCode.getContextObjects(), IsMapContaining.hasKey("dummy"));
		
		String json = authCode.getContextObjects().get("dummy");
		DummyContext ctx = this.mapper.readValue(json, DummyContext.class);
		
		assertThat(ctx.getName(), equalTo("CN=Lionel/O=USER"));
	}
	
	/**
	 * Normal jws
	 */
	@Test
	public void whenNormalJws_thenOK() throws Exception {
		// Create an access token
		AccessToken accessToken = new AccessToken() {{
			setAud("1234");
			setIss(coreIss);
			setSub("CN=Lionel/O=USER");
			setExpires(timeSvc.currentTimeSeconds() + 10L);
		}};
		String jws = this.jwtSvc.createJws(accessToken, "xx");
		
		// Read the token back again
		AccessToken accessTokenDeser = this.jwtSvc.fromJws(jws, "xx", AccessToken.class);
		assertThat(accessTokenDeser.getAud(), equalTo("1234"));
		assertThat(accessTokenDeser.getSub(), equalTo("CN=Lionel/O=USER"));
		assertThat(accessTokenDeser.getIss(), equalTo(this.coreIss));
		assertThat(accessTokenDeser.getExpires(), equalTo(timeSvc.currentTimeSeconds() + 10L));
	}
}
