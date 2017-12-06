package com.github.lhervier.domino.oauth.server.services.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ex.grant.GrantServerErrorException;
import com.github.lhervier.domino.oauth.server.ext.OAuthExtension;
import com.github.lhervier.domino.oauth.server.ext.OAuthProperty;
import com.github.lhervier.domino.oauth.server.ext.TokenResponse;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.services.ExtensionService;
import com.github.lhervier.domino.oauth.server.services.JWTService;
import com.github.lhervier.domino.oauth.server.utils.Utils;

/**
 * Base class for grant services
 * @author Lionel HERVIER
 */
public class BaseGrantService {

	/**
	 * The extension service
	 */
	@Autowired
	private ExtensionService extSvc;
	
	/**
	 * JWT service
	 */
	@Autowired
	private JWTService jwtSvc;
	
	public Map<String, Object> extractProperties(
			NotesPrincipal user,
			Application app,
			AuthCodeEntity authCode) throws GrantServerErrorException {
		Map<String, Object> resp = new HashMap<String, Object>();
		for( String responseType : authCode.getResponseTypes() ) {
			OAuthExtension ext = this.extSvc.getExtension(responseType);
			Object context = Utils.getContext(authCode, responseType);		// May be null
			TokenResponse response = ext.token(
					user,
					app,
					context, 
					authCode.getGrantedScopes()
			);
			if( response == null )
				continue;
			
			// Save properties, detecting conflicts
			for( OAuthProperty prop : response.getProperties().values() ) {
				if( resp.containsKey(prop.getName()) ) {
					throw new GrantServerErrorException("Extension conflicts on setting properties");
				} else if( prop.getSignKey() == null ) {
					resp.put(prop.getName(), prop.getValue());
				} else {
					resp.put(prop.getName(), this.jwtSvc.createJws(prop.getValue(), prop.getSignKey()));
				}
			}
		}
		return resp;
	}
}
