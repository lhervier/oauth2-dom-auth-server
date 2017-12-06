package com.github.lhervier.domino.oauth.server.services;

public interface JWTService {

	public String createJws(Object obj, String signKey);
}
