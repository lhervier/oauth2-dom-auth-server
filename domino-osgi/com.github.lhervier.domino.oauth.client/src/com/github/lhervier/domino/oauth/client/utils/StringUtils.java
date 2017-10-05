package com.github.lhervier.domino.oauth.client.utils;

public class StringUtils {

	public static final boolean isEmpty(String s) {
		if( s == null )
			return true;
		return s.length() == 0;
	}
}
