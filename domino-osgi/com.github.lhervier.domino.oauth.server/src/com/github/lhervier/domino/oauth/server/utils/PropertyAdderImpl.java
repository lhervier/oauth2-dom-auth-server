package com.github.lhervier.domino.oauth.server.utils;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import com.github.lhervier.domino.oauth.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.server.repo.SecretRepository;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;

public class PropertyAdderImpl implements IPropertyAdder {

	/**
	 * Map to add properties
	 */
	private Map<String, Object> dest;
	
	/**
	 * Secret repository
	 */
	private SecretRepository secretRepo;
	
	/**
	 * The jackson mapper
	 */
	private ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * Constructeur
	 */
	public PropertyAdderImpl(
			Map<String, Object> dest, 
			SecretRepository secretRepo) {
		this.dest = dest;
		this.secretRepo = secretRepo;
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IPropertyAdder#addCryptedProperty(String, Object, String)
	 */
	@Override
	public void addCryptedProperty(String name, Object obj, String kid) {
		if( dest.containsKey(name) )
			throw new RuntimeException("La propriété '" + name + "' est déjà définie dans la réponse au grant.");
		
		try {
			byte[] secret = this.secretRepo.findCryptSecret(kid);
			JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128GCM, null, null, null, null, null, null, null, null, null, kid, null, null, null, null, null, 0, null, null, null, null);
			JWEObject jweObject = new JWEObject(
					header, 
					new Payload(this.mapper.writeValueAsString(obj))
			);
			jweObject.encrypt(new DirectEncrypter(secret));
			String jwe = jweObject.serialize();
			this.dest.put(name, jwe);
		} catch (KeyLengthException e) {
			throw new RuntimeException(e);
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IPropertyAdder#addSignedProperty(String, Object, String)
	 */
	@Override
	public void addSignedProperty(String name, Object obj, String kid) {
		if( dest.containsKey(name) )
			throw new RuntimeException("La propriété '" + name + "' est déjà définie dans la réponse au grant.");
		
		try {
			byte[] secret = this.secretRepo.findSignSecret(kid);
			JWSHeader header = new JWSHeader(JWSAlgorithm.HS256, null, null, null, null, null, null, null, null, null, kid, null, null);
			JWSObject jwsObject = new JWSObject(
					header,
	                new Payload(this.mapper.writeValueAsString(obj))
			);
			jwsObject.sign(new MACSigner(secret));
			String jws = jwsObject.serialize();
			
			this.dest.put(name, jws);
		} catch (KeyLengthException e) {
			throw new RuntimeException(e);
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IPropertyAdder#addProperty(java.lang.String, java.lang.Object)
	 */
	@Override
	public void addProperty(String name, Object value) {
		this.dest.put(name, value);
	}

}
