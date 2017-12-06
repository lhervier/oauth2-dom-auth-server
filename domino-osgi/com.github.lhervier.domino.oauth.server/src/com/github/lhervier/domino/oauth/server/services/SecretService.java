package com.github.lhervier.domino.oauth.server.services;

public interface SecretService {

	public String createJws(Object obj, String signKey);
}
