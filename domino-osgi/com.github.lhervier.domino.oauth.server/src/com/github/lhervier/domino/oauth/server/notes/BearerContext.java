package com.github.lhervier.domino.oauth.server.notes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.ext.core.AccessToken;
import com.github.lhervier.domino.oauth.server.services.JWTService;
import com.ibm.domino.napi.NException;
import com.ibm.domino.napi.c.NotesUtil;
import com.ibm.domino.napi.c.Os;
import com.ibm.domino.napi.c.xsp.XSPNative;
import com.ibm.domino.osgi.core.context.ContextInfo;

import lotus.domino.NotesException;
import lotus.domino.Session;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BearerContext {

	/**
	 * The http request
	 */
	@Autowired
	private HttpServletRequest request;
	
	/**
	 * JWT service
	 */
	@Autowired
	private JWTService jwtSvc;
	
	/**
	 * The sign key
	 */
	@Value("${oauth2.server.core.signKey}")
	private String signKey;
	
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
	 * Bearer header sent
	 */
	private boolean headerSent = false;
	
	/**
	 * Bean initialization
	 */
	@PostConstruct
	public void init() {
		this.bearerSession = null;
		try {
			// Only work when at server root
			if( ContextInfo.getUserDatabase() != null )
				return;
			
			// Extract bearer token
			String auth = this.request.getHeader("Authorization");
			if( auth == null )
				return;
			if( !auth.startsWith("Bearer ") )
				return;
			String sJws = auth.substring("Bearer ".length());
			this.headerSent = true;
			
			// Get the access token
			AccessToken accessToken = this.jwtSvc.fromJws(sJws, signKey, AccessToken.class);
			if( accessToken == null )
				return;
			
			// Extract token information
			this.clientId = accessToken.getAud();
			if( accessToken.getScope() == null )
				this.scopes = new ArrayList<String>();
			else
				this.scopes = Arrays.asList(StringUtils.split(accessToken.getScope(), ' '));
			
			// Create session
			String userName = accessToken.getSub();
			this.userNameList = NotesUtil.createUserNameList(userName);
			this.bearerSession = XSPNative.createXPageSession(userName, this.userNameList, false, false);
		} catch (NotesException e) {
			throw new RuntimeException(e);
		} catch (NException e) {
			throw new RuntimeException(e);
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
	
	/**
	 * Return true if bearer authentication is used
	 */
	public boolean isBearerAuth() {
		return this.headerSent;
	}
}
