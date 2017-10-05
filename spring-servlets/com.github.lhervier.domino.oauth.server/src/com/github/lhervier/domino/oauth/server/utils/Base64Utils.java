package com.github.lhervier.domino.oauth.server.utils;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

public class Base64Utils {

	/**
	 * Encode un buffer en base 64
	 * @param buff le buffer à encoder
	 * @return la chaîne en base 64
	 */
	public static final String encode(byte[] buff) {
		try {
			return new String(Base64.encodeBase64(buff), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Encode une chaîne UTF-8
	 * @param s la chaîne
	 * @return la version base64
	 */
	public static final String encodeFromUTF8String(String s) {
		try {
			return encode(s.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Décode une chaîne base 64
	 * @param s la chaîne à décoder
	 * @return le buffer
	 */
	public static final byte[] decode(String s) {
		try {
			return Base64.decodeBase64(s.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Décode une chaîne base 64 vers une autre chaîne UTF-8
	 * @param s la chaîne à décoder
	 * @return la chaîne décodée
	 */
	public static final String decodeToUTF8String(String s) {
		try {
			return new String(decode(s), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
