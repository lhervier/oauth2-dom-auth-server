package com.github.lhervier.domino.oauth.library.client.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.github.lhervier.domino.oauth.common.utils.JSFUtils;
import com.github.lhervier.domino.oauth.library.client.bean.AccessTokenBean;
import com.github.lhervier.domino.oauth.library.client.bean.ParamsBean;

public class Utils {

	/**
	 * Retourne la bean de paramétrage
	 * @return la bean de paramétrage
	 */
	public static final ParamsBean getParamsBean() {
		return (ParamsBean) JSFUtils.getBean("paramsBean");
	}
	
	/**
	 * Retourne la bean pour accéder au token
	 */
	public static final AccessTokenBean getAccessTokenBean() {
		return (AccessTokenBean) JSFUtils.getBean("accessTokenBean");
	}
	
	/**
	 * Retourne l'URL de redirection
	 * @return l'url de redirection
	 * @throws UnsupportedEncodingException 
	 */
	public static final String getEncodedRedirectUri() throws UnsupportedEncodingException {
		ParamsBean paramsBean = Utils.getParamsBean();
		StringBuffer redirectUri = new StringBuffer();
		redirectUri.append(paramsBean.getBaseURI());
		if( !paramsBean.getBaseURI().endsWith("/") )
			redirectUri.append('/');
		redirectUri.append("init.xsp");
		return URLEncoder.encode(redirectUri.toString(), "UTF-8");
	}
}
