package com.github.lhervier.domino.oauth.client.utils;

public interface Callback<T> {

	public void run(T obj) throws Exception;
	
}
