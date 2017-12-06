package com.github.lhervier.domino.oauth.server.testsuite.impl;

import java.io.UnsupportedEncodingException;

import com.github.lhervier.domino.oauth.server.repo.SecretRepository;

public class SecretRepositoryTestImpl implements SecretRepository {

	public static final String INITIAL_SIGN_KEY = "98765432109876543210987654321098";
	public static final String INITIAL_CRYPT_KEY = "0123456789012345";
	
	public static String CRYPT_KEY = INITIAL_CRYPT_KEY;
	public static String SIGN_KEY = INITIAL_SIGN_KEY;
	
	@Override
	public byte[] findCryptSecret(String ssoConfig) {
		try {
			return CRYPT_KEY.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] findSignSecret(String ssoConfig) {
		try {
			return SIGN_KEY.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
