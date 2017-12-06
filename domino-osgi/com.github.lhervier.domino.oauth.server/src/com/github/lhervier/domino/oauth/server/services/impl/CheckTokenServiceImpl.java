package com.github.lhervier.domino.oauth.server.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ext.core.AccessToken;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.model.TokenContent;
import com.github.lhervier.domino.oauth.server.services.CheckTokenService;
import com.github.lhervier.domino.oauth.server.services.JWTService;

/**
 * Service to check for tokens
 * @author Lionel HERVIER
 */
@Service
public class CheckTokenServiceImpl implements CheckTokenService {

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
	 * @see com.github.lhervier.domino.oauth.server.services.CheckTokenService#checkToken(Application, String)
	 */
	public TokenContent checkToken(
			Application userApp, 
			String token) throws NotAuthorizedException {
		AccessToken tk = this.jwtSvc.fromJws(token, this.signKey, AccessToken.class);
		
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
			resp.setScope(tk.getScope());
			resp.setSub(tk.getSub());
			resp.setIss(tk.getIss());
			// resp.setAud(tk.getAud());						// OPTIONAL. Will make Spring Security calls fail...
		}
		return resp;
	}
}
