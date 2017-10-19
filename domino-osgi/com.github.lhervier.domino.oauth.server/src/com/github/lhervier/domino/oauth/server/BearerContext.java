package com.github.lhervier.domino.oauth.server;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;

import lotus.domino.NotesException;
import lotus.domino.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.ext.core.AccessToken;
import com.github.lhervier.domino.oauth.server.services.SecretService;
import com.github.lhervier.domino.oauth.server.utils.DominoUtils;
import com.github.lhervier.domino.oauth.server.utils.SystemUtils;
import com.ibm.domino.napi.NException;
import com.ibm.domino.napi.c.NotesUtil;
import com.ibm.domino.napi.c.Os;
import com.ibm.domino.napi.c.xsp.XSPNative;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BearerContext {

	/**
	 * Logger
	 */
	private static final Log LOG = LogFactory.getLog(BearerContext.class);
	
	/**
	 * The http request
	 */
	@Autowired
	private HttpServletRequest request;
	
	/**
	 * Service to get the secrets
	 */
	@Autowired
	private SecretService secretSvc;
	
	/**
	 * The sign key
	 */
	@Value("${oauth2.server.core.signKey}")
	private String signKey;
	
	/**
	 * Object mapper
	 */
	private ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * Delegated
	 */
	private Session bearerSession;

	/**
	 * The usernamelist
	 */
	private Long userNameList;
	
	/**
	 * The clientId
	 */
	private String clientId;
	
	/**
	 * The scopes
	 */
	private List<String> scopes;
	
	/**
	 * Bean initialization
	 */
	@PostConstruct
	public void init() {
		this.bearerSession = null;
		try {
			// Extract bearer token
			String auth = this.request.getHeader("Authorization");
			if( auth == null )
				return;
			if( !auth.startsWith("Bearer ") )
				return;
			String sJws = auth.substring("Bearer ".length());
			
			// Parse the JWT
			JWSObject jwsObj = JWSObject.parse(sJws);
			
			// Check sign key
			String kid = jwsObj.getHeader().getKeyID();
			if( !this.signKey.equals(kid) ) {
				LOG.error("kid incorrect in Bearer token");
				return;
			}
			
			// Check algorithm
			String alg = jwsObj.getHeader().getAlgorithm().getName();
			if( !"HS256".equals(alg) ) {
				LOG.error("alg incorrect in Bearer token");
				return;
			}
			
			// Check signature
			byte[] secret = this.secretSvc.getSignSecret(kid);
			JWSVerifier verifier = new MACVerifier(secret);
			if( !jwsObj.verify(verifier) ) {
				LOG.error("Bearer token verification failed");
				return;
			}
			
			// Deserialize token
			AccessToken accessToken = this.mapper.readValue(jwsObj.getPayload().toString(), AccessToken.class);
			
			// Check token is not expired
			if( accessToken.getExp() < SystemUtils.currentTimeSeconds() ) {
				LOG.error("Bearer token expired");
				return;
			}
			
			// Extract token information
			this.clientId = accessToken.getAud();
			this.scopes = accessToken.getScopes();
			
			// Create session
			String userName = accessToken.getSub();
			this.userNameList = NotesUtil.createUserNameList(userName);
			this.bearerSession = XSPNative.createXPageSession(userName, this.userNameList, false, false);
		} catch (NotesException e) {
			throw new RuntimeException(e);
		} catch (NException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		} catch (ParseException e) {
			return;
		}
	}
	
	/**
	 * Bean cleaup
	 */
	@PreDestroy
	public void cleanUp() {
		if( this.bearerSession != null ) {
			DominoUtils.recycleQuietly(this.bearerSession);
			this.bearerSession = null;
		}
		
		if( this.userNameList != null ) {
			try {
				Os.OSMemFree(this.userNameList);
			} catch (NException e) {
				throw new RuntimeException(e);
			}
			this.userNameList = null;
		}
	}
	
	/**
	 * Return the scopes
	 */
	public List<String> getBearerScopes() {
		return this.scopes;
	}
	
	/**
	 * Return the clientId
	 */
	public String getBearerClientId() {
		return this.clientId;
	}
	
	/**
	 * Return the bearer session
	 */
	public Session getBearerSession() {
		return this.bearerSession;
	}
}