package com.github.lhervier.domino.oauth.library.client.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import lotus.domino.NotesException;

import com.github.lhervier.domino.oauth.common.model.GrantResponse;
import com.github.lhervier.domino.oauth.common.model.error.GrantError;
import com.github.lhervier.domino.oauth.common.utils.HttpUtils;
import com.github.lhervier.domino.oauth.common.utils.JSFUtils;
import com.github.lhervier.domino.oauth.library.client.Constants;
import com.github.lhervier.domino.oauth.library.client.bean.AccessTokenBean;
import com.github.lhervier.domino.oauth.library.client.bean.InitParamsBean;

public class Utils {

	/**
	 * Est ce que le contexte SSL a été initialisé ?
	 */
	private static boolean sslInitialized = false;
	
	/**
	 * Est ce qu'on doit désactiver la vérification des certificats SSL
	 */
	private static boolean disableCheckCertificate = false;
	
	/**
	 * Retourne la bean de paramétrage
	 * @return la bean de paramétrage
	 */
	public static final InitParamsBean getInitParamsBean() {
		return (InitParamsBean) JSFUtils.getBean("initParamsBean");
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
		InitParamsBean paramsBean = Utils.getInitParamsBean();
		StringBuffer redirectUri = new StringBuffer();
		redirectUri.append(paramsBean.getBaseURI());
		if( !paramsBean.getBaseURI().endsWith("/") )
			redirectUri.append('/');
		redirectUri.append("init.xsp");
		return URLEncoder.encode(redirectUri.toString(), "UTF-8");
	}
	
	/**
	 * Pour initialiser le contexte SSL.
	 * On ne l'initialise qu'une seule fois. Pour revenir en arrière, il
	 * faut relancer la tâche http.
	 * @return le contexte SSL
	 */
	private final static synchronized SSLSocketFactory getSSLSocketFactory() {
		// On a déjà initialisé le contexte
		if( sslInitialized ) {
			if( !disableCheckCertificate )
				return null;
			
			SSLContext context;
			try {
				context = SSLContext.getInstance("SSL");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
			return context.getSocketFactory();
		}
		
		// A partie de là, SSL sera initialisé
		sslInitialized = true;
		disableCheckCertificate = disableCheckCertificate();
		
		// On ne doit pas désactiver la vérification
		if( !disableCheckCertificate ) {
			return null;
		}
		
		// On doit désactiver la vérification
		// Ca se fait au niveau de la JVM. Impossible de revenir dessus.
		try {
			SSLContext context = SSLContext.getInstance("SSL");
			TrustManager[] trustAll = new TrustManager[] { new X509TrustManager() {
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
	            }
	            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
	            }
	        }};
			context.init(null, trustAll, new SecureRandom());
			return context.getSocketFactory();
		} catch (KeyManagementException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Est ce qu'on doit désactiver les certificats SSL ?
	 */
	private static final boolean disableCheckCertificate() {
		try {
			String s = JSFUtils.getSessionAsSigner().getEnvironmentString(
					Constants.NOTES_INI_DISABLE_CHECK_CERTIFICATE, 
					true
			);
			if( s == null || s.length() == 0 )
				return false;
			return Boolean.parseBoolean(s);
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Pour initialiser un GET
	 * @param url l'url
	 * @return la connection
	 */
	public static final HttpUtils<GrantResponse, GrantError> createConnection(String url) {
		InitParamsBean paramsBean = getInitParamsBean();
		
		HostnameVerifier verifier = null;
		if( paramsBean.isDisableHostNameVerifier() ) {
			verifier = new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
		}
		
		return HttpUtils.createConnection(url, GrantResponse.class, GrantError.class)
				.addHeader("Authorization", "Basic " + paramsBean.getSecret())
				.withVerifier(verifier)
				.withFactory(getSSLSocketFactory());
	}
}
