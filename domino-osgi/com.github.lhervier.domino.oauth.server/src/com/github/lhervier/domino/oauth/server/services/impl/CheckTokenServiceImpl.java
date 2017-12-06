package com.github.lhervier.domino.oauth.server.services.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.server.ext.core.AccessToken;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.model.TokenContent;
import com.github.lhervier.domino.oauth.server.services.AppService;
import com.github.lhervier.domino.oauth.server.services.CheckTokenService;
import com.github.lhervier.domino.oauth.server.services.JWTService;

/**
 * Service to check for tokens
 * @author Lionel HERVIER
 */
@Service
public class CheckTokenServiceImpl implements CheckTokenService {

	/**
	 * Logger
	 */
	private static final Log LOG = LogFactory.getLog(CheckTokenServiceImpl.class);
	
	/**
	 * App service
	 */
	@Autowired
	private AppService appService;
	
	/**
	 * JWT Service
	 */
	@Autowired
	private JWTService jwtSvc;
	
	/**
	 * SSO config used to sign access tokens
	 */
	@Value("${oauth2.server.core.signKey}")
	private String signKey;
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.CheckTokenService#checkToken(com.github.lhervier.domino.oauth.server.NotesPrincipal, java.lang.String)
	 */
	public TokenContent checkToken(
			NotesPrincipal user, 
			String token) throws NotAuthorizedException {
		// User must be logged in as an application
		Application userApp = this.appService.getApplicationFromName(user.getCommon());
		if( userApp == null ) {
			LOG.error("Not logged in as an application");
			throw new NotAuthorizedException();
		}
		
		AccessToken tk;
		try {
			tk = this.jwtSvc.fromJws(token, this.signKey, AccessToken.class);
		} catch (ServerErrorException e) {
			throw new NotAuthorizedException();
		}
		
		TokenContent resp = new TokenContent();
		if( tk == null )
			resp.setActive(false);
		else {
			resp.setActive(true);
			resp.setClientId(tk.getAud());
			resp.setExp(tk.getExpires());
			resp.setTokenType("bearer");
			resp.setUsername(tk.getSub());
			resp.setSpringUsername(resp.getUsername());			// Spring OAUTH2 Security will look at the "user_name" property insted of the "username" property (as defined in RFC7662)
			resp.setScope(StringUtils.join(tk.getScopes().iterator(), ' '));
			resp.setSub(tk.getSub());
			resp.setIss(tk.getIss());
			// resp.setAud(tk.getAud());						// OPTIONAL. Will make Spring Security calls fail...
		}
		return resp;
	}
}
