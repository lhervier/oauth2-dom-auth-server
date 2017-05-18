package com.github.lhervier.domino.oauth.common.utils;

import java.io.IOException;

public interface Callback<T> {

	public void run(T obj) throws IOException;
	
}
