package com.github.lhervier.domino.oauth.server.testsuite;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

public class TestUtils {

	public static final String urlDecode(String s) {
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static final Map<String, String> urlRefs(String s) throws MalformedURLException {
		return parseQuery(new URL(s).getRef());
	}
	public static final Map<String, String> urlParameters(String s) throws MalformedURLException {
		return parseQuery(new URL(s).getQuery());
	}
	private static final Map<String, String> parseQuery(String query) {
		Map<String, String> ret = new HashMap<String, String>();
		if( StringUtils.isEmpty(query) )
			return ret;
		String[] parameters = query.split("&");
		for( String parameter : parameters ) {
			String[] pair = parameter.split("=");
			String key = pair[0];
			String value;
			if( pair.length == 2 )
				value = urlDecode(pair[1]);
			else
				value = "";
			ret.put(key, value);
		}
		return ret;
	}
}
