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

import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ex.BaseGrantException;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.authorize.AuthServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.grant.GrantInvalidGrantException;
import com.github.lhervier.domino.oauth.server.ex.grant.GrantInvalidScopeException;
import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.repo.SecretRepository;
import com.github.lhervier.domino.oauth.server.services.AuthCodeService;
import com.github.lhervier.domino.oauth.server.services.ExtensionService;
import com.github.lhervier.domino.oauth.server.services.GrantService;
import com.github.lhervier.domino.oauth.server.services.TimeService;
import com.github.lhervier.domino.oauth.server.utils.PropertyAdderImpl;
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
	 * Secret repository
	 */
	@Autowired
	private SecretRepository secretRepo;
	
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, Object> refreshToken(
			Application app,
			String refreshToken, 
			List<String> scopes) throws BaseGrantException, ServerErrorException {
		// Sanity check
		if( refreshToken == null )
			throw new GrantInvalidGrantException("refresh_token is mandatory");
		
		// Decrypt refresh token
		AuthCodeEntity authCode = this.authCodeSvc.toEntity(refreshToken);
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
		if( !app.getClientId().equals(authCode.getClientId()) )
			throw new GrantInvalidGrantException("invalid client_id");
		
		// Prepare the response
		Map<String, Object> resp = new HashMap<String, Object>();
		
		// Call for extensions
		for( IOAuthExtension ext : this.extSvc.getExtensions() ) {
			Object context = Utils.getContext(authCode, ext.getId());
			if( context == null )
				continue;
			ext.token(
					context, 
					new PropertyAdderImpl(
							resp,
							this.secretRepo
					), 
					scopes
			);
		}
		
		// Update scopes. Here, we have granted all the asked scopes (otherwise, we would have thrown).
		authCode.setGrantedScopes(scopes);
		
		// Regenerate the refresh token (update expiration date)
		authCode.setExpires(this.timeSvc.currentTimeSeconds() + this.refreshTokenLifetime);
		String newRefreshToken = this.authCodeSvc.fromEntity(authCode);
		resp.put("refresh_token", newRefreshToken);
		
		// Other information
		resp.put("expires_in", this.refreshTokenLifetime);
		resp.put("token_type", "Bearer");
		
		return resp;
	}
	
	
}
