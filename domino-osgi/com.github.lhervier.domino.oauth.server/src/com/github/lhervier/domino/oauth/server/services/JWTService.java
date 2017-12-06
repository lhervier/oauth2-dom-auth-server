package com.github.lhervier.domino.oauth.server.services;

public interface JWTService {

	public String createJws(Object obj, String signConfig);
	
	public String createJwe(Object obj, String cryptConfig);
	
	public <T> T fromJws(String jws, String signKey, Class<T> cl);
	
	public <T> T fromJwe(String jwe, String cryptKey, Class<T> cl);
}
