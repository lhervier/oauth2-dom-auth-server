package com.github.lhervier.domino.oauth.server.services;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lotus.domino.NotesException;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ex.GrantException;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.authorize.AuthorizeServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.grant.InvalidGrantException;
import com.github.lhervier.domino.oauth.server.ex.grant.InvalidScopeException;
import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.repo.SecretRepository;
import com.github.lhervier.domino.oauth.server.utils.PropertyAdderImpl;
import com.github.lhervier.domino.oauth.server.utils.SystemUtils;
import com.github.lhervier.domino.oauth.server.utils.Utils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.DirectDecrypter;

@Service("refresh_token")
public class RefreshTokenGrantService extends BaseGrantService {

	/**
	 * The refresh token life time
	 */
	@Value("${oauth2.server.refreshTokenLifetime}")
	private long refreshTokenLifetime;
	
	/**
	 * Secret repository
	 */
	@Autowired
	private SecretRepository secretRespo;
	
	/**
	 * The extensions
	 */
	@SuppressWarnings("unchecked")
	@Autowired
	private List<IOAuthExtension> exts;
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.BaseGrantService#createGrant(com.github.lhervier.domino.oauth.server.model.Application, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Map<String, Object> createGrant(
			Application app, 
			String grantType,
			String code, 
			String scope, 
			String refreshToken, 
			String redirectUri) throws GrantException, ServerErrorException, NotesException {
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
	 * @param sRefreshToken le refresh token
	 * @param scopes d'éventuels nouveaux scopes.
	 * @throws GrantException 
	 * @throws AuthorizeServerErrorException
	 * @throws NotesException
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> refreshToken(
			Application app,
			String sRefreshToken, 
			List<String> scopes) throws GrantException, ServerErrorException, NotesException {
		// Sanity check
		if( sRefreshToken == null )
			throw new InvalidGrantException("refresh_token is mandatory");
		
		// Decrypt refresh token
		AuthCodeEntity authCode = this.authCodeFromRefreshToken(sRefreshToken);
		if( authCode == null )
			throw new InvalidGrantException("refresh_token is invalid");
			
		// Check that scopes are already in the initial scopes
		if( !authCode.getGrantedScopes().containsAll(scopes) )
			throw new InvalidScopeException("scopes must be a subset of already accorded scopes");
		
		// If no scope, use the scopes originally granted by the resource owner 
		if( scopes.size() == 0 )
			scopes = authCode.getGrantedScopes();
		
		// Check that the token has been generated for the current application
		if( !app.getClientId().equals(authCode.getClientId()) )
			throw new InvalidGrantException();
		
		// Prepare the response
		Map<String, Object> resp = new HashMap<String, Object>();
		
		// Call for extensions
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
					scopes
			);
		}
		
		// Update scopes
		if( scopes.size() != 0 ) {
			if( !scopes.containsAll(authCode.getGrantedScopes())) {
				resp.put("scope", StringUtils.join(scopes.iterator(), " "));
				authCode.setGrantedScopes(scopes);
			}
		}
		
		// Regenerate the refresh token
		String newRefreshToken = this.refreshTokenFromAuthCode(authCode);
		resp.put("refresh_token", newRefreshToken);
		
		// Other information
		resp.put("expires_in", this.refreshTokenLifetime);
		resp.put("token_type", "Bearer");
		
		return resp;
	}
	
	/**
	 * Extract a refresh token
	 */
	private AuthCodeEntity authCodeFromRefreshToken(String sRefreshToken) throws ServerErrorException {
		try {
			JWEObject jweObject = JWEObject.parse(sRefreshToken);
			jweObject.decrypt(new DirectDecrypter(this.secretRespo.findRefreshTokenSecret()));
			JSONObject payload = jweObject.getPayload().toJSONObject();
			
			// Check it is not expired
			if( payload.getAsNumber("exp").longValue() < SystemUtils.currentTimeSeconds() )
				return null;
			
			AuthCodeEntity ret = new AuthCodeEntity();
			ret.setId(payload.getAsString("id"));
			ret.setApplication(payload.getAsString("application"));
			ret.setClientId(payload.getAsString("clientId"));
			ret.setRedirectUri(payload.getAsString("redirectUri"));
			
			JSONArray scopes = (JSONArray) payload.get("scopes");
			ret.setScopes(new ArrayList<String>());
			for( Object scope : scopes )
				ret.getScopes().add((String) scope);
			
			JSONArray grantedScopes = (JSONArray) payload.get("grantedScopes");
			ret.setGrantedScopes(new ArrayList<String>());
			for( Object scope : grantedScopes )
				ret.getGrantedScopes().add((String) scope);
			
			JSONObject contexts = (JSONObject) payload.get("contexts");
			ret.setContextClasses(new HashMap<String, String>());
			ret.setContextObjects(new HashMap<String, String>());
			for( String extId : contexts.keySet() ) {
				JSONObject context = (JSONObject) contexts.get(extId);
				ret.getContextClasses().put(extId, context.getAsString("clazz"));
				ret.getContextObjects().put(extId, context.getAsString("jsonValue"));
			}
			
			return ret;
		} catch (KeyLengthException e) {
			throw new ServerErrorException(e);
		} catch (NotesException e) {
			throw new ServerErrorException(e);
		} catch (JOSEException e) {
			throw new ServerErrorException(e);
		} catch (IOException e) {
			throw new ServerErrorException(e);
		} catch (ParseException e) {
			return null;
		}
	}
}
