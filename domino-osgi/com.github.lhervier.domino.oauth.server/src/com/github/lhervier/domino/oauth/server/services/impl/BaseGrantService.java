package com.github.lhervier.domino.oauth.server.services.impl;

import java.io.IOException;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.server.repo.SecretRepository;
import com.github.lhervier.domino.oauth.server.services.GrantService;
import com.github.lhervier.domino.oauth.server.services.TimeService;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectEncrypter;

/**
 * Base class to manage the grant requests
 * @author Lionel HERVIER
 */
public abstract class BaseGrantService implements GrantService {

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
	private SecretRepository secretRespo;
	
	/**
	 * Time service
	 */
	@Autowired
	private TimeService timeSvc;
	
	/**
	 * Generate a new refresh token
	 * @param authCode the authorization code
	 * @return the refresh token
	 * @throws IOException 
	 * @throws JOSEException 
	 * @throws KeyLengthException 
	 */
	protected String refreshTokenFromAuthCode(AuthCodeEntity authCode) throws ServerErrorException {
		try {
			JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128GCM);
			
			JSONObject payload = new JSONObject();
			payload.put("id", authCode.getId());
			payload.put("exp", this.timeSvc.currentTimeSeconds() + this.refreshTokenLifetime);
			payload.put("application", authCode.getApplication());
			payload.put("clientId", authCode.getClientId());
			payload.put("redirectUri", authCode.getRedirectUri());
			
			JSONArray scopes = new JSONArray();
			for( String scope : authCode.getScopes() )
				scopes.add(scope);
			payload.put("scopes", scopes);
			
			JSONArray grantedScopes = new JSONArray();
			for( String scope : authCode.getGrantedScopes() )
				grantedScopes.add(scope);
			payload.put("grantedScopes", grantedScopes);
			
			JSONObject contexts = new JSONObject();
			for( String extId : authCode.getContextClasses().keySet() ) {
				JSONObject context = new JSONObject();
				context.put(
						"clazz", 
						authCode.getContextClasses().get(extId)
				);
				context.put(
						"jsonValue", 
						authCode.getContextObjects().get(extId)
				);
				contexts.put(extId, context);
			}
			payload.put("contexts", contexts);
			
			JWEObject jweObject = new JWEObject(
					header, 
					new Payload(payload)
			);
			jweObject.encrypt(new DirectEncrypter(
					this.secretRespo.findCryptSecret(this.refreshTokenConfig)
			));
			return jweObject.serialize();
		} catch (KeyLengthException e) {
			throw new ServerErrorException(e);
		} catch (JOSEException e) {
			throw new ServerErrorException(e);
		}
	}
}
