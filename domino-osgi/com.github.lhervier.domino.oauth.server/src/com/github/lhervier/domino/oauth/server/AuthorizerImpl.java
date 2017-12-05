package com.github.lhervier.domino.oauth.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.ext.IAuthorizer;
import com.github.lhervier.domino.oauth.server.repo.SecretRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AuthorizerImpl implements IAuthorizer {

	/**
	 * Secret repository
	 */
	@Autowired
	private SecretRepository secretRepo;
	
	/**
	 * Object mapper
	 */
	@Autowired
	private ObjectMapper mapper;
	
	/**
	 * Should we save auth code ?
	 */
	private Boolean saveAuthCode = null;
	
	/**
	 * List of signed properties
	 */
	private Map<String, String> signedProperties = new HashMap<String, String>();
	
	/**
	 * List of norma properties
	 */
	private Map<String, Object> properties = new HashMap<String, Object>();
	
	/**
	 * Context object
	 */
	private Object context;
	
	/**
	 * Is there a conflict for saving the auth code ?
	 */
	private boolean saveAuthConflict = false;
	
	/**
	 * Is there a conflict in the properties generated by extensions ?
	 */
	private boolean propertiesConflict = false;
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IAuthorizer#saveAuthCode(boolean)
	 */
	@Override
	public void saveAuthCode(boolean save) {
		if( saveAuthCode != null )
			this.saveAuthConflict = true;
		else
			this.saveAuthCode = save;
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IPropertyAdder#addSignedProperty(java.lang.String, java.lang.Object, java.lang.String)
	 */
	@Override
	public void addSignedProperty(String name, Object obj, String kid) {
		if( this.signedProperties.containsKey(name) || this.properties.containsKey(name) ) {
			this.propertiesConflict = true;
			return;
		}
		
		try {
			byte[] secret = this.secretRepo.findSignSecret(kid);
			JWSHeader header = new JWSHeader(JWSAlgorithm.HS256, null, null, null, null, null, null, null, null, null, kid, null, null);
			JWSObject jwsObject = new JWSObject(
					header,
	                new Payload(this.mapper.writeValueAsString(obj))
			);
			jwsObject.sign(new MACSigner(secret));
			String jws = jwsObject.serialize();
			
			this.signedProperties.put(name, jws);
		} catch (KeyLengthException e) {
			throw new RuntimeException(e);
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IPropertyAdder#addProperty(String, Object)
	 */
	@Override
	public void addProperty(String name, Object value) {
		if( this.signedProperties.containsKey(name) || this.properties.containsKey(name) ) {
			this.propertiesConflict = true;
			return;
		}
		
		this.properties.put(name, value);
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.ext.IAuthorizer#setContext(java.lang.Object)
	 */
	@Override
	public void setContext(Object context) {
		this.context = context;
	}
	
	// ==========================================================================================

	/**
	 * @return the saveAuthCode
	 */
	public Boolean isSaveAuthCode() {
		return saveAuthCode;
	}

	/**
	 * @return the signedProperties
	 */
	public Map<String, String> getSignedProperties() {
		return signedProperties;
	}

	/**
	 * @return the properties
	 */
	public Map<String, Object> getProperties() {
		return properties;
	}

	/**
	 * @return the context
	 */
	public Object getContext() {
		return context;
	}

	/**
	 * @return the saveAuthConflict
	 */
	public boolean isSaveAuthConflict() {
		return saveAuthConflict;
	}

	/**
	 * @return the propertiesConflict
	 */
	public boolean isPropertiesConflict() {
		return propertiesConflict;
	}
}
