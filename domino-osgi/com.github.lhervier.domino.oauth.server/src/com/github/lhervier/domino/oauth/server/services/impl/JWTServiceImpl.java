package com.github.lhervier.domino.oauth.server.services.impl;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.repo.SecretRepository;
import com.github.lhervier.domino.oauth.server.services.JWTService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;

@Service
public class JWTServiceImpl implements JWTService {

	@Autowired
	private SecretRepository secretRepo;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Override
	public String createJws(Object obj, String signKey) {
		try {
			byte[] secret = this.secretRepo.findSignSecret(signKey);
			JWSHeader header = new JWSHeader(JWSAlgorithm.HS256, null, null, null, null, null, null, null, null, null, signKey, null, null);
			JWSObject jwsObject = new JWSObject(
					header,
	                new Payload(this.mapper.writeValueAsString(obj))
			);
			jwsObject.sign(new MACSigner(secret));
			return jwsObject.serialize();
		} catch (KeyLengthException e) {
			throw new RuntimeException(e);
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
