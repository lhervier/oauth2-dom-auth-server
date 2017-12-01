package com.github.lhervier.domino.oauth.server.utils;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import com.github.lhervier.domino.oauth.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.server.repo.SecretRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
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
	private ObjectMapper mapper;
	
	/**
	 * Constructeur
	 */
	public PropertyAdderImpl(
			Map<String, Object> dest, 
			SecretRepository secretRepo,
			ObjectMapper mapper) {
		this.dest = dest;
		this.secretRepo = secretRepo;
		this.mapper = mapper;
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IPropertyAdder#addSignedProperty(String, Object, String)
	 */
	@Override
	public void addSignedProperty(String name, Object obj, String kid) {
		if( dest.containsKey(name) )
			throw new RuntimeException("La propri�t� '" + name + "' est d�j� d�finie dans la r�ponse au grant.");
		
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
