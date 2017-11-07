package com.github.lhervier.domino.oauth.server.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lotus.domino.NotesException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ex.GrantException;
import com.github.lhervier.domino.oauth.server.ex.InvalidUriException;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.grant.InvalidClientException;
import com.github.lhervier.domino.oauth.server.ex.grant.InvalidGrantException;
import com.github.lhervier.domino.oauth.server.ex.grant.InvalidRequestException;
import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.repo.AuthCodeRepository;
import com.github.lhervier.domino.oauth.server.repo.SecretRepository;
import com.github.lhervier.domino.oauth.server.utils.PropertyAdderImpl;
import com.github.lhervier.domino.oauth.server.utils.SystemUtils;
import com.github.lhervier.domino.oauth.server.utils.Utils;

@Service("authorization_code")
public class AuthCodeGrantService extends BaseGrantService {

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
	 * The refresh token life time
	 */
	@Value("${oauth2.server.refreshTokenLifetime}")
	private long refreshTokenLifetime;
	
	/**
	 * The extensions
	 */
	@SuppressWarnings("unchecked")
	@Autowired
	private List<IOAuthExtension> exts;
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.BaseGrantService#createGrant(Application, String, String, String, String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> createGrant(
			Application app,
			String grantType, 
			String code, 
			String scope,
			String refreshToken, 
			String redirectUri) throws GrantException, ServerErrorException, NotesException {
		// Validate redirect_uri
		try {
			Utils.checkRedirectUri(redirectUri, app);
		} catch (InvalidUriException e) {
			throw new ServerErrorException(e.getMessage());
		}
		
		// Validate the code
		if( code == null )
			throw new InvalidRequestException("code is mandatory");
		
		// Process authorization code
		try {
			// Prepare the response
			Map<String, Object> resp = new HashMap<String, Object>();
		
			// Get the authorization code
			AuthCodeEntity authCode = this.authCodeRepo.findOne(code);
			if( authCode == null )
				throw new InvalidGrantException();
			
			// Check it did not expire
			long expired = (long) authCode.getExpires();
			if( expired < SystemUtils.currentTimeSeconds() )
				throw new InvalidGrantException("code has expired");
			
			// Check it was generated for the right clientId
			if( !app.getClientId().equals(authCode.getClientId()) )
				throw new InvalidClientException("client_id is not the same as the one stored in the authorization code");
			
			// Check that the redirect_uri is the same
			if( !redirectUri.equals(authCode.getRedirectUri()) )
				throw new InvalidGrantException("redirect_uri is not the same as the one stored in the authorization code");
			
			// Make each implementation add its own properties
			// They can change their context.
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
			if( !authCode.getScopes().containsAll(authCode.getGrantedScopes()) )
				resp.put("scope", StringUtils.join(authCode.getGrantedScopes().iterator(), " "));
			
			return resp;
		} finally {
			// Remove auth code to prevend reuse
			this.authCodeRepo.delete(code);
		}
	}
}
