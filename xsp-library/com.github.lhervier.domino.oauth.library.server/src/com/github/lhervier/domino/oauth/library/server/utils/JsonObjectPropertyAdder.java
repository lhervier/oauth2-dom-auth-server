package com.github.lhervier.domino.oauth.library.server.utils;

import java.io.IOException;

import lotus.domino.NotesException;

import com.github.lhervier.domino.oauth.common.utils.GsonUtils;
import com.github.lhervier.domino.oauth.library.server.bean.SecretBean;
import com.github.lhervier.domino.oauth.library.server.ext.IPropertyAdder;
import com.google.gson.JsonObject;
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

public class JsonObjectPropertyAdder implements IPropertyAdder {

	/**
	 * L'objet dans lequel ajouter
	 */
	private JsonObject dest;
	
	/**
	 * La clé à utiliser pour signer
	 */
	private String signKey;
	
	/**
	 * La clé à utiliser pour crypter
	 */
	private String cryptKey;
	
	/**
	 * La bean pour accéder aux secrets
	 */
	private SecretBean secretBean;
	
	/**
	 * Constructeur
	 */
	public JsonObjectPropertyAdder(
			JsonObject dest, 
			SecretBean secretBean,
			String signKey,
			String cryptKey) {
		this.dest = dest;
		this.signKey = signKey;
		this.cryptKey = cryptKey;
		this.secretBean = secretBean;
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IPropertyAdder#addCryptedProperty(java.lang.String, com.google.gson.JsonObject)
	 */
	@Override
	public void addCryptedProperty(String name, JsonObject obj) {
		if( dest.has(name) )
			throw new RuntimeException("La propriété '" + name + "' est déjà définie dans la réponse au grant.");
		
		try {
			byte[] secret = this.secretBean.getCryptSecret(this.cryptKey);
			JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128GCM, null, null, null, null, null, null, null, null, null, this.cryptKey, null, null, null, null, null, 0, null, null, null, null);
			JWEObject jweObject = new JWEObject(
					header, 
					new Payload(GsonUtils.toJson(obj))
			);
			jweObject.encrypt(new DirectEncrypter(secret));
			String jwe = jweObject.serialize();
			this.dest.addProperty(name, jwe);
		} catch (KeyLengthException e) {
			throw new RuntimeException(e);
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		} catch(IOException e) {
			throw new RuntimeException(e);
		} catch (NotesException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IPropertyAdder#addSignedProperty(java.lang.String, com.google.gson.JsonObject)
	 */
	@Override
	public void addSignedProperty(String name, JsonObject obj) {
		if( dest.has(name) )
			throw new RuntimeException("La propriété '" + name + "' est déjà définie dans la réponse au grant.");
		
		try {
			byte[] secret = this.secretBean.getSignSecret(this.signKey);
			JWSHeader header = new JWSHeader(JWSAlgorithm.HS256, null, null, null, null, null, null, null, null, null, this.signKey, null, null);
			JWSObject jwsObject = new JWSObject(
					header,
	                new Payload(GsonUtils.toJson(obj))
			);
			jwsObject.sign(new MACSigner(secret));
			String jws = jwsObject.serialize();
			
			this.dest.addProperty(name, jws);
		} catch (KeyLengthException e) {
			throw new RuntimeException(e);
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		} catch (NotesException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
