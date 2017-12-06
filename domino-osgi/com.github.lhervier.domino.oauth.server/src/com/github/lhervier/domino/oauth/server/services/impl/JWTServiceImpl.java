package com.github.lhervier.domino.oauth.server.services.impl;

import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.IExpirable;
import com.github.lhervier.domino.oauth.server.repo.SecretRepository;
import com.github.lhervier.domino.oauth.server.services.JWTService;
import com.github.lhervier.domino.oauth.server.services.TimeService;
import com.github.lhervier.domino.oauth.server.utils.Utils;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;

@Service
public class JWTServiceImpl implements JWTService {
	
	private static final Log LOG = LogFactory.getLog(JWTServiceImpl.class);
	
	@Autowired
	private SecretRepository secretRepo;
	
	@Autowired
	private TimeService timeSvc;
	
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

	/**
	 * @see com.github.lhervier.domino.oauth.server.services.JWTService#createJwe(java.lang.Object, java.lang.String)
	 */
	@Override
	public String createJwe(Object obj, String cryptConfig) {
		try {
			JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128GCM);
			
			// Create JWE
			String json = this.mapper.writeValueAsString(obj);
			JWEObject jweObject = new JWEObject(
					header, 
					new Payload(json)
			);
			jweObject.encrypt(new DirectEncrypter(
					this.secretRepo.findCryptSecret(cryptConfig)
			));
			return jweObject.serialize();
		} catch (KeyLengthException e) {
			throw new RuntimeException(e);
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		} catch (JsonGenerationException e) {
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.services.JWTService#fromJws(java.lang.String, java.lang.String, java.lang.Class)
	 */
	@Override
	public <T> T fromJws(String jws, String signConfig, Class<T> cl) {
		try {
			JWSObject jwsObj = JWSObject.parse(jws);
			
			String kid = jwsObj.getHeader().getKeyID();
			if( !Utils.equals(signConfig, kid) ) {
				LOG.error("kid incorrect in Bearer token");
				return null;
			}
			
			String alg = jwsObj.getHeader().getAlgorithm().getName();
			if( !Utils.equals("HS256", alg) ) {
				LOG.error("alg incorrect in Bearer token");
				return null;
			}
			
			byte[] secret = this.secretRepo.findSignSecret(kid);
			JWSVerifier verifier = new MACVerifier(secret);
			if( !jwsObj.verify(verifier) ) {
				LOG.error("Bearer token verification failed");
				return null;
			}
			
			T obj = this.mapper.readValue(jwsObj.getPayload().toString(), cl);
			
			if( obj instanceof IExpirable ) {
				IExpirable exp = (IExpirable) obj;
				if( exp.getExpires() < this.timeSvc.currentTimeSeconds() ) {
					LOG.error("Bearer token expired");
					return null;
				}
			}
			
			return obj;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.services.JWTService#fromJwe(java.lang.String, java.lang.String, java.lang.Class)
	 */
	@Override
	public <T> T fromJwe(String jwe, String cryptConfig, Class<T> cl) {
		try {
			JWEObject jweObject = JWEObject.parse(jwe);
			jweObject.decrypt(
					new DirectDecrypter(
							this.secretRepo.findCryptSecret(cryptConfig)
					)
			);
			String json = jweObject.getPayload().toString();
			T obj = this.mapper.readValue(json, cl);
			
			if( obj instanceof IExpirable ) {
				IExpirable exp = (IExpirable) obj;
				if( exp.getExpires() < this.timeSvc.currentTimeSeconds() )
					return null;
			}
			
			return obj;
		} catch (ParseException e) {
			return null;								// Invalid JWE
		} catch (JsonParseException e) {
			throw new RuntimeException(e);				// Invalid json in JWE. May not happen (we generated the JWE)
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

	
}
