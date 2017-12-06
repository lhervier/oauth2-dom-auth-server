package com.github.lhervier.domino.oauth.server.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.AuthCodeNotesPrincipal;
import com.github.lhervier.domino.oauth.server.AuthorizerImpl;
import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ex.BaseGrantException;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.authorize.AuthServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.grant.GrantInvalidGrantException;
import com.github.lhervier.domino.oauth.server.ex.grant.GrantInvalidScopeException;
import com.github.lhervier.domino.oauth.server.ex.grant.GrantServerErrorException;
import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.services.AuthCodeService;
import com.github.lhervier.domino.oauth.server.services.ExtensionService;
import com.github.lhervier.domino.oauth.server.services.GrantService;
import com.github.lhervier.domino.oauth.server.services.TimeService;
import com.github.lhervier.domino.oauth.server.utils.Utils;

@Service("refresh_token")
public class RefreshTokenGrantServiceImpl implements GrantService {

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
	 * The auth code service
	 */
	@Autowired
	private AuthCodeService authCodeSvc;
	
	/**
	 * The time service
	 */
	@Autowired
	private TimeService timeSvc;
	
	/**
	 * The extension service
	 */
	@Autowired
	private ExtensionService extSvc;
	
	/**
	 * The request
	 */
	@Autowired
	private HttpServletRequest request;
	
	/**
	 * The authorizer
	 */
	@Autowired
	private AuthorizerImpl authorizer;
	
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
	public Map<String, Object> createGrant(
			Application app,
			String scope,
			String refreshToken) throws BaseGrantException, ServerErrorException {
		// Extract scopes
		List<String> scopes;
		if( StringUtils.isEmpty(scope) )
			scopes = new ArrayList<String>();
		else
			scopes = Arrays.asList(StringUtils.split(scope, " "));
		
		// Refresh the token
		return this.refreshToken(
				app,
				refreshToken,
				scopes
		);
	}

	/**
	 * Refresh a token
	 * @param app the currently logged in user (application)
	 * @param refreshToken le refresh token
	 * @param scopes d'éventuels nouveaux scopes.
	 * @throws BaseGrantException 
	 * @throws AuthServerErrorException
	 */
	private Map<String, Object> refreshToken(
			Application app,
			String refreshToken, 
			List<String> scopes) throws BaseGrantException, ServerErrorException {
		// Sanity check
		if( refreshToken == null )
			throw new GrantInvalidGrantException("refresh_token is mandatory");
		
		// Decrypt refresh token
		final AuthCodeEntity authCode = this.authCodeSvc.toEntity(refreshToken);
		if( authCode == null )
			throw new GrantInvalidGrantException("invalid refresh_token");
		
		// Check that token has not expired
		if( authCode.getExpires() < this.timeSvc.currentTimeSeconds() )
			throw new GrantInvalidGrantException("invalid refresh_token");
		
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
		Map<String, Object> resp = new HashMap<String, Object>();
		for( String responseType : this.extSvc.getResponseTypes() ) {
			IOAuthExtension ext = this.extSvc.getExtension(responseType);
			Object context = Utils.getContext(authCode, responseType);		// May be null
			ext.token(
					user,
					app,
					context, 
					authCode.getGrantedScopes(),
					this.authorizer
			);
		}
		if( this.authorizer.isPropertiesConflict() )
			throw new GrantServerErrorException("Extension conflicts on setting properties");
		
		// Add properties to response
		resp.putAll(this.authorizer.getSignedProperties());
		resp.putAll(this.authorizer.getProperties());
		
		// Regenerate the refresh token (update expiration date)
		// When using this grant, clientType is confidential.
		authCode.setExpires(this.timeSvc.currentTimeSeconds() + this.refreshTokenLifetime);
		String newRefreshToken = this.authCodeSvc.fromEntity(authCode);
		resp.put("refresh_token", newRefreshToken);
		
		// Other information
		resp.put("expires_in", this.refreshTokenLifetime);
		
		return resp;
	}
	
	
}
