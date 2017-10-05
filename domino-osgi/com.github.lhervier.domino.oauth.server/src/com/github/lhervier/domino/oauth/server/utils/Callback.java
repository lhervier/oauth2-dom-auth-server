package com.github.lhervier.domino.oauth.server.utils;

public interface Callback<T> {

	public void run(T obj) throws Exception;
	
}
