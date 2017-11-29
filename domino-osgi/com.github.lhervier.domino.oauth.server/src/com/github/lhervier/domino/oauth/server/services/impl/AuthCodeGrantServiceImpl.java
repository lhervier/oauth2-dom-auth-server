package com.github.lhervier.domino.oauth.server.services.impl;

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
import com.github.lhervier.domino.oauth.server.ex.grant.GrantInvalidClientException;
import com.github.lhervier.domino.oauth.server.ex.grant.GrantInvalidGrantException;
import com.github.lhervier.domino.oauth.server.ex.grant.GrantInvalidRequestException;
import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.repo.AuthCodeRepository;
import com.github.lhervier.domino.oauth.server.repo.SecretRepository;
import com.github.lhervier.domino.oauth.server.services.TimeService;
import com.github.lhervier.domino.oauth.server.utils.PropertyAdderImpl;
import com.github.lhervier.domino.oauth.server.utils.Utils;

@Service("authorization_code")
public class AuthCodeGrantServiceImpl extends BaseGrantService {

	/**
	 * Authorization code repository
	 */
	@Autowired
	private AuthCodeRepository authCodeRepo;
	
	/**
	 * Secret repository
	 */
	@Autowired
	private SecretRepository secretRespo;
	
	/**
	 * Time service
	 */
	@Autowired
	private TimeService timeSvc;
	
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
	 * The extensions
	 */
	@SuppressWarnings({ "rawtypes" })
	@Autowired
	private List<IOAuthExtension> exts;
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.GrantService#createGrant(com.github.lhervier.domino.oauth.server.model.Application, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Map<String, Object> createGrant(Application app) throws BaseGrantException, ServerErrorException {
		return this.createGrant(
				app, 
				this.request.getParameter("redirect_uri"),
				this.request.getParameter("code")
		);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> createGrant(Application app, String redirectUri, String code) throws BaseGrantException, ServerErrorException {
		// Get URI from app if it only have one
		if( StringUtils.isEmpty(redirectUri) )
			if( app.getRedirectUris() == null || app.getRedirectUris().size() == 0 )
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
			AuthCodeEntity authCode = this.authCodeRepo.findOne(code);
			if( authCode == null )
				throw new GrantInvalidGrantException("code is invalid");
			
			// Check it did not expire
			long expired = (long) authCode.getExpires();
			if( expired < this.timeSvc.currentTimeSeconds() )
				throw new GrantInvalidGrantException("code has expired");
			
			// Check it was generated for the right clientId
			if( !app.getClientId().equals(authCode.getClientId()) )
				throw new GrantInvalidClientException("code generated for another app");
			
			// Check that the redirect_uri is the same
			if( !redirectUri.equals(authCode.getRedirectUri()) )
				throw new GrantInvalidGrantException("invalid redirect_uri : It is not the same as the one stored in the authorization code");
			
			// Make each implementation add its own properties
			// They can change their context.
			Map<String, Object> resp = new HashMap<String, Object>();
			for( IOAuthExtension ext : this.exts ) {
				Object context = Utils.getContext(authCode, ext.getId());
				if( context == null )
					continue;
				ext.token(
						context, 
						new PropertyAdderImpl(
								resp, 
								this.secretRespo
						),
						authCode.getGrantedScopes()
				);
			}
			
			// Generate the refresh token
			String sRefreshToken = this.refreshTokenFromAuthCode(authCode);
			resp.put("refresh_token", sRefreshToken);
			
			// expiration date
			resp.put("expires_in", this.refreshTokenLifetime);
			
			// token type
			resp.put("token_type", "Bearer");
			
			// scopes only if they are different from the one asked when calling authorize end point
			if( !authCode.getGrantedScopes().containsAll(authCode.getScopes()) )
				resp.put("scope", StringUtils.join(authCode.getGrantedScopes().iterator(), " "));
			
			return resp;
		} finally {
			// Remove auth code to prevend reuse
			this.authCodeRepo.delete(code);
		}
	}
}
