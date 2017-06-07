package com.github.lhervier.domino.oauth.common.utils;

public interface Callback<T> {

	public void run(T obj) throws Exception;
	
}
