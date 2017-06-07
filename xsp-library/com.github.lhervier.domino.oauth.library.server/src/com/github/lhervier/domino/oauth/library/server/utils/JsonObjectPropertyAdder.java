package com.github.lhervier.domino.oauth.library.server.utils;

import com.github.lhervier.domino.oauth.common.utils.GsonUtils;
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
	private byte[] signKey;
	
	/**
	 * La clé à utiliser pour crypter
	 */
	private byte[] cryptKey;
	
	/**
	 * Constructeur
	 * @param dest l'objet dans lequel ajouter les propriétés
	 */
	public JsonObjectPropertyAdder(
			JsonObject dest, 
			byte[] signKey,
			byte[] cryptKey) {
		this.dest = dest;
		this.signKey = signKey;
		this.cryptKey = cryptKey;
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IPropertyAdder#addCryptedProperty(java.lang.String, com.google.gson.JsonObject)
	 */
	@Override
	public void addCryptedProperty(String name, JsonObject obj) {
		if( dest.has(name) )
			throw new RuntimeException("La propriété '" + name + "' est déjà définie dans la réponse au grant.");
		
		JWEObject jweObject = new JWEObject(
				new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128GCM), 
				new Payload(GsonUtils.toJson(obj))
		);
		try {
			jweObject.encrypt(new DirectEncrypter(this.cryptKey));
		} catch (KeyLengthException e) {
			throw new RuntimeException(e);
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		}
		String jwe = jweObject.serialize();
		
		this.dest.addProperty(name, jwe);
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IPropertyAdder#addSignedProperty(java.lang.String, com.google.gson.JsonObject)
	 */
	@Override
	public void addSignedProperty(String name, JsonObject obj) {
		if( dest.has(name) )
			throw new RuntimeException("La propriété '" + name + "' est déjà définie dans la réponse au grant.");
		
		JWSObject jwsObject = new JWSObject(
				new JWSHeader(JWSAlgorithm.HS256),
                new Payload(GsonUtils.toJson(obj))
		);
		try {
			jwsObject.sign(new MACSigner(this.signKey));
		} catch (KeyLengthException e) {
			throw new RuntimeException(e);
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		}
		String jws = jwsObject.serialize();
		
		this.dest.addProperty(name, jws);
	}

}
