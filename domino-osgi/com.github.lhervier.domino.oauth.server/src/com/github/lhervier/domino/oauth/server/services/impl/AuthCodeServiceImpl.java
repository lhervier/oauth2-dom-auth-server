package com.github.lhervier.domino.oauth.server.services.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.server.repo.SecretRepository;
import com.github.lhervier.domino.oauth.server.services.AuthCodeService;
import com.github.lhervier.domino.oauth.server.services.TimeService;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Service
public class AuthCodeServiceImpl implements AuthCodeService {

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
	 * The secret repo
	 */
	@Autowired
	private SecretRepository secretRepo;
	
	/**
	 * The time service
	 */
	@Autowired
	private TimeService timeSvc;
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.AuthCodeService#toEntity(java.lang.String)
	 */
	public AuthCodeEntity toEntity(String sRefreshToken) throws ServerErrorException {
		try {
			JWEObject jweObject = JWEObject.parse(sRefreshToken);
			jweObject.decrypt(
					new DirectDecrypter(
							this.secretRepo.findCryptSecret(this.refreshTokenConfig)
					)
			);
			JSONObject payload = jweObject.getPayload().toJSONObject();
			
			// Check it is not expired
			if( payload.getAsNumber("exp").longValue() < this.timeSvc.currentTimeSeconds() )
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
		} catch (JOSEException e) {
			throw new ServerErrorException(e);
		} catch (ParseException e) {
			return null;
		}
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.AuthCodeService#fromEntity(com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity)
	 */
	public String fromEntity(AuthCodeEntity authCode) throws ServerErrorException {
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
					this.secretRepo.findCryptSecret(this.refreshTokenConfig)
			));
			return jweObject.serialize();
		} catch (KeyLengthException e) {
			throw new ServerErrorException(e);
		} catch (JOSEException e) {
			throw new ServerErrorException(e);
		}
	}
}
