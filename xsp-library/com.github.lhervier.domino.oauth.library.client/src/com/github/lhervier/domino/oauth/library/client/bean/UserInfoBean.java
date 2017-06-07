package com.github.lhervier.domino.oauth.library.client.bean;

import java.io.IOException;

import com.github.lhervier.domino.oauth.common.utils.JSFUtils;

public class UserInfoBean {

	/**
	 * Retourne les infos utilisateur
	 * @throws IOException 
	 */
	public void userInfo() throws IOException {
		JSFUtils.sendJson(JSFUtils.getSessionScope().get("id_token"));
	}
}
