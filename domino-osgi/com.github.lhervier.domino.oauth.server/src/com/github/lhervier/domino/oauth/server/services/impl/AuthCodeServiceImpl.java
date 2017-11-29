package com.github.lhervier.domino.oauth.server.services.impl;

import java.io.IOException;
import java.text.ParseException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
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

@Service
public class AuthCodeServiceImpl implements AuthCodeService {

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
	 * Jackson mapper
	 */
	private ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.AuthCodeService#toEntity(java.lang.String)
	 */
	public AuthCodeEntity toEntity(String refreshToken) throws ServerErrorException {
		try {
			JWEObject jweObject = JWEObject.parse(refreshToken);
			jweObject.decrypt(
					new DirectDecrypter(
							this.secretRepo.findCryptSecret(this.refreshTokenConfig)
					)
			);
			String json = jweObject.getPayload().toString();
			AuthCodeEntity entity = this.mapper.readValue(json, AuthCodeEntity.class);
			
			if( entity.getExpires() < this.timeSvc.currentTimeSeconds() )
				return null;
			
			return entity;
		} catch (JsonParseException e) {
			throw new RuntimeException(e);				// Invalid json in JWE. May not happen (we generated the JWE)
		} catch (ParseException e) {
			return null;								// Invalid JWE
		} catch (JsonMappingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (KeyLengthException e) {
			throw new RuntimeException(e);
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.AuthCodeService#fromEntity(com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity)
	 */
	public String fromEntity(AuthCodeEntity authCode) throws ServerErrorException {
		try {
			JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128GCM);
			
			// Create JWE
			String json = this.mapper.writeValueAsString(authCode);
			JWEObject jweObject = new JWEObject(
					header, 
					new Payload(json)
			);
			jweObject.encrypt(new DirectEncrypter(
					this.secretRepo.findCryptSecret(this.refreshTokenConfig)
			));
			return jweObject.serialize();
		} catch (KeyLengthException e) {
			throw new ServerErrorException(e);
		} catch (JOSEException e) {
			throw new ServerErrorException(e);
		} catch (JsonGenerationException e) {
			throw new ServerErrorException(e);
		} catch (JsonMappingException e) {
			throw new ServerErrorException(e);
		} catch (IOException e) {
			throw new ServerErrorException(e);
		}
	}
}
