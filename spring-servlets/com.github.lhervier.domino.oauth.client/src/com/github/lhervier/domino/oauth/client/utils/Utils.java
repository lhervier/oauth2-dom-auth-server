package com.github.lhervier.domino.oauth.client.utils;

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

import lotus.domino.Session;

import com.github.lhervier.domino.oauth.client.Constants;
import com.github.lhervier.domino.oauth.client.model.GrantError;
import com.github.lhervier.domino.oauth.client.model.GrantResponse;

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
	 * URL encode a value
	 * @param value 
	 * @return the encoded value
	 */
	public static final String urlEncode(String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e);		// UTF-8 is supported !
		}
	}
	
	/**
	 * Retourne l'URL de redirection
	 * @param baseUri
	 * @return l'url de redirection
	 * @throws UnsupportedEncodingException 
	 */
	public static final String getEncodedRedirectUri(String baseUri) {
		StringBuffer redirectUri = new StringBuffer();
		redirectUri.append(baseUri);
		if( !baseUri.endsWith("/") )
			redirectUri.append('/');
		redirectUri.append("oauth2-client/init");
		return urlEncode(redirectUri.toString());
	}
	
	/**
	 * Pour initialiser le contexte SSL.
	 * On ne l'initialise qu'une seule fois. Pour revenir en arrière, il
	 * faut relancer la tâche http.
	 * @param session a session that allows us to access the notes.ini
	 * @return le contexte SSL
	 */
	private final static synchronized SSLSocketFactory getSSLSocketFactory(Session session) {
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
		disableCheckCertificate = disableCheckCertificate(session);
		
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
	 * @param session a session that allows us to access the notes.ini
	 */
	private static final boolean disableCheckCertificate(Session session) {
		return DominoUtils.getEnvironment(
				session, 
				Constants.NOTES_INI_DISABLE_CHECK_CERTIFICATE, 
				Boolean.class,
				false
		);
	}
	
	/**
	 * Pour initialiser un GET
	 * @param session a session that allows us to access the notes.ini
	 * @param disableHostVerifier 
	 * @param url l'url
	 * @return la connection
	 */
	public static final HttpConnection<GrantResponse, GrantError> createConnection(
			Session session, 
			boolean disableHostVerifier, 
			String secret,
			String url) {
		HostnameVerifier verifier = null;
		if( disableHostVerifier ) {
			verifier = new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
		}
		
		return HttpConnection.createConnection(url, GrantResponse.class, GrantError.class)
				.addHeader("Authorization", "Basic " + secret)
				.addHeader("Content-Type", "application/x-www-form-urlencoded")
				.withVerifier(verifier)
				.withFactory(getSSLSocketFactory(session));
	}
}
