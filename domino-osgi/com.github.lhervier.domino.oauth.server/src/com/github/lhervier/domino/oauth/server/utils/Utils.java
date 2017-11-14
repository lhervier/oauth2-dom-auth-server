package com.github.lhervier.domino.oauth.server.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ex.InvalidUriException;
import com.github.lhervier.domino.oauth.server.model.Application;

public class Utils {

	/**
	 * Securely generate random numbers
	 */
	private static final SecureRandom RANDOM = new SecureRandom();
	
	/**
	 * Jackson mapper
	 */
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	/**
	 * Check the redirectUri
	 */
	public static final void checkRedirectUri(String redirectUri, Application app) throws InvalidUriException {
		if( StringUtils.isEmpty(redirectUri) )
			throw new InvalidUriException("No redirect_uri in query string.");
		try {
			URI uri = new URI(redirectUri);
			if( !uri.isAbsolute() )
				throw new InvalidUriException("Invalid redirect_uri. Must be absolute.");
			
			// Check uri is declared in the application
			Set<String> redirectUris = new HashSet<String>();
			redirectUris.add(app.getRedirectUri().toString());
			for( String u : app.getRedirectUris() )
				redirectUris.add(u);
			if( !redirectUris.contains(redirectUri) )
				throw new InvalidUriException("redirect_uri '" + redirectUri + "' is not declared in the uris of application '" + app.getClientId() + "'");
		} catch (URISyntaxException e) {
			throw new InvalidUriException("Invalid redirect_uri. Syntax is invalid.");
		}
	}
	
	/**
	 * Create an random id
	 * @return l'identifiant
	 */
	public static final String generateCode() {
		// see https://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
		return new BigInteger(260, RANDOM).toString(32);
	}
	
	/**
	 * Return a context object
	 * @param code the authorization code
	 * @param IOAuthExtension the extension to extract context for
	 */
	public static final Object getContext(AuthCodeEntity authCode, String extId) {
		try {
			String className = authCode.getContextClasses().get(extId);
			if( className == null )
				return null;
			Class<?> contextClass = Class.forName(className);
			
			String json = authCode.getContextObjects().get(extId);
			if( json == null )
				return null;
			return MAPPER.readValue(json, contextClass);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (JsonParseException e) {
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * URLEncode a string, without throwing an IOException
	 * @param s the string to url encode
	 * @return the url encoded value
	 * @return
	 */
	public static final String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);			// UTF-8 is supported !!
		}
	}
}
