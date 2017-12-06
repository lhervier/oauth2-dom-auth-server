package com.github.lhervier.domino.oauth.server.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.AuthCodeNotesPrincipal;
import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ex.BaseGrantException;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.grant.GrantInvalidGrantException;
import com.github.lhervier.domino.oauth.server.ex.grant.GrantInvalidScopeException;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.services.GrantService;
import com.github.lhervier.domino.oauth.server.services.JWTService;
import com.github.lhervier.domino.oauth.server.services.TimeService;
import com.github.lhervier.domino.oauth.server.utils.Utils;

@Service("refresh_token")
public class RefreshTokenGrantServiceImpl extends BaseGrantService implements GrantService {

	/**
	 * The refresh token life time
	 */
	@Value("${oauth2.server.refreshTokenLifetime}")
	private long refreshTokenLifetime;
	
	/**
	 * Name of the LTPA config used to encrypt refresh tokens
	 */
	@Value("${oauth2.server.refreshTokenConfig}")
	private String refreshTokenConfig;
	
	/**
	 * The JWT service
	 */
	@Autowired
	private JWTService jwtSvc;
	
	/**
	 * The time service
	 */
	@Autowired
	private TimeService timeSvc;
	
	/**
	 * The request
	 */
	@Autowired
	private HttpServletRequest request;
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.GrantService#createGrant(com.github.lhervier.domino.oauth.server.model.Application, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Map<String, Object> createGrant(Application app) throws BaseGrantException, ServerErrorException {
		return this.createGrant(
				app,
				this.request.getParameter("scope"),
				this.request.getParameter("refresh_token")
		);
	}
	public Map<String, Object> createGrant(Application app, String scope, String refreshToken) throws BaseGrantException, ServerErrorException {
		// Sanity check
		if( refreshToken == null )
			throw new GrantInvalidGrantException("refresh_token is mandatory");
		
		// Decrypt refresh token (null if expired)
		final AuthCodeEntity authCode = this.jwtSvc.fromJwe(refreshToken, this.refreshTokenConfig, AuthCodeEntity.class);
		if( authCode == null )
			throw new GrantInvalidGrantException("invalid refresh_token");
		
		// Extract scopes
		List<String> scopes;
		if( StringUtils.isEmpty(scope) )
			scopes = new ArrayList<String>();
		else
			scopes = Arrays.asList(StringUtils.split(scope, " "));
		
		// Check that scopes are already in the initial scopes
		if( !authCode.getGrantedScopes().containsAll(scopes) )
			throw new GrantInvalidScopeException("invalid scope: Must be a subset of already granted scopes");
		
		// If no scope, use the scopes originally granted by the resource owner 
		if( scopes.size() == 0 )
			scopes = authCode.getGrantedScopes();
		
		// Check that the token has been generated for the current application
		if( !Utils.equals(app.getClientId(), authCode.getClientId()) )
			throw new GrantInvalidGrantException("invalid client_id");
		
		// Update scopes. Here, we have granted all the asked scopes (otherwise, we would have thrown).
		authCode.setGrantedScopes(scopes);
		
		// Extract the user from the auth code
		NotesPrincipal user = new AuthCodeNotesPrincipal(authCode);
		
		// Call for extensions
		Map<String, Object> resp = this.extractProperties(user, app, authCode);
		
		// Regenerate the refresh token (update expiration date)
		// When using this grant, clientType is confidential.
		authCode.setExpires(this.timeSvc.currentTimeSeconds() + this.refreshTokenLifetime);
		String newRefreshToken = this.jwtSvc.createJwe(authCode, this.refreshTokenConfig);
		resp.put("refresh_token", newRefreshToken);
		
		// Other information
		resp.put("expires_in", this.refreshTokenLifetime);
		
		return resp;
	}
	
}
