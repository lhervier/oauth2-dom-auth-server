package com.github.lhervier.domino.oauth.server.services.impl;

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
import com.github.lhervier.domino.oauth.server.ex.grant.GrantInvalidClientException;
import com.github.lhervier.domino.oauth.server.ex.grant.GrantInvalidGrantException;
import com.github.lhervier.domino.oauth.server.ex.grant.GrantInvalidRequestException;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.model.ClientType;
import com.github.lhervier.domino.oauth.server.repo.AuthCodeRepository;
import com.github.lhervier.domino.oauth.server.services.GrantService;
import com.github.lhervier.domino.oauth.server.services.JWTService;
import com.github.lhervier.domino.oauth.server.services.TimeService;
import com.github.lhervier.domino.oauth.server.utils.Utils;

@Service("authorization_code")
public class AuthCodeGrantServiceImpl extends BaseGrantService implements GrantService {

	/**
	 * Authorization code repository
	 */
	@Autowired
	private AuthCodeRepository authCodeRepo;
	
	/**
	 * Time service
	 */
	@Autowired
	private TimeService timeSvc;
	
	/**
	 * JWT service
	 */
	@Autowired
	private JWTService jwtSvc;
	
	/**
	 * Request
	 */
	@Autowired
	private HttpServletRequest request;
	
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
	 * @see com.github.lhervier.domino.oauth.server.services.GrantService#createGrant(com.github.lhervier.domino.oauth.server.model.Application, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Map<String, Object> createGrant(Application app) throws BaseGrantException {
		return this.createGrant(
				app, 
				this.request.getParameter("redirect_uri"),
				this.request.getParameter("code")
		);
	}
	public Map<String, Object> createGrant(Application app, String redirectUri, String code) throws BaseGrantException {
		// Get URI from app if it only have one
		if( StringUtils.isEmpty(redirectUri) )
			if( app.getRedirectUris().isEmpty() )
				redirectUri = app.getRedirectUri();
		
		// RedirectURI must not be empty
		if( StringUtils.isEmpty(redirectUri) )
			throw new GrantInvalidRequestException("redirect_uri is mandatory");
		
		// Validate the code
		if( StringUtils.isEmpty(code) )
			throw new GrantInvalidRequestException("code is mandatory");
		
		// Process authorization code
		try {
			// Get the authorization code
			final AuthCodeEntity authCode = this.authCodeRepo.findOne(code);
			if( authCode == null )
				throw new GrantInvalidGrantException("invalid auth code");
			
			// Check it did not expire
			if( authCode.getExpires() < this.timeSvc.currentTimeSeconds() )
				throw new GrantInvalidGrantException("code has expired");
			
			// Check it was generated for the right clientId
			if( !Utils.equals(app.getClientId(), authCode.getClientId()) )
				throw new GrantInvalidClientException("code generated for another app");
			
			// Check that the redirect_uri is the same
			if( !Utils.equals(redirectUri, authCode.getRedirectUri()) )
				throw new GrantInvalidGrantException("invalid redirect_uri : It is not the same as the one stored in the authorization code");
			
			// Extract the user from the auth code
			NotesPrincipal user = new AuthCodeNotesPrincipal(authCode);
			
			// Make each implementation add its own properties
			// They can change their context.
			Map<String, Object> resp = this.extractProperties(user, app, authCode);
			
			// Generate the refresh token only for confidential clients
			if( ClientType.CONFIDENTIAL == app.getClientType() ) {
				authCode.setExpires(this.timeSvc.currentTimeSeconds() + this.refreshTokenLifetime);
				String sRefreshToken = this.jwtSvc.createJwe(authCode, this.refreshTokenConfig);
				resp.put("refresh_token", sRefreshToken);
			}
			
			// scopes only if they are different from the one asked when calling authorize end point
			if( !authCode.getGrantedScopes().containsAll(authCode.getScopes()) )
				resp.put("scope", StringUtils.join(authCode.getGrantedScopes().iterator(), " "));
			
			return resp;
		} finally {
			// Remove auth code to prevent reuse
			this.authCodeRepo.delete(code);
		}
	}
}
