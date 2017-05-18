package com.github.lhervier.domino.oauth.library.client.bean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.github.lhervier.domino.oauth.common.utils.JSFUtils;

/**
 * Bean pour retourner l'access token
 * @author Lionel HERVIER
 */
public class AccessTokenBean {

	/**
	 * Envoi l'access token en Json dans la stream http
	 * @throws IOException
	 */
	public void sendToken() throws IOException {
		Map<String, Object> token = new HashMap<String, Object>();
		token.put("access_token", JSFUtils.getSessionScope().get("access_token"));
		JSFUtils.sendJson(token);
	}
}
