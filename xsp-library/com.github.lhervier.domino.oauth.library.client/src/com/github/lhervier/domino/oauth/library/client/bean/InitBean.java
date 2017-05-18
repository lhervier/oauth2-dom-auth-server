package com.github.lhervier.domino.oauth.library.client.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.github.lhervier.domino.oauth.common.model.GrantResponse;
import com.github.lhervier.domino.oauth.common.model.error.GrantError;
import com.github.lhervier.domino.oauth.common.utils.GsonUtils;
import com.github.lhervier.domino.oauth.common.utils.JSFUtils;
import com.github.lhervier.domino.oauth.library.client.utils.Utils;

public class InitBean {

	/**
	 * La bean pour accéder au paramétrage
	 */
	private ParamsBean paramsBean;
	
	/**
	 * Constructeur
	 */
	public InitBean() {
		this.paramsBean = Utils.getParamsBean();
	}
	
	/**
	 * Initialisation
	 * @throws IOException 
	 * @throws JsonDeserializeException 
	 */
	public void init() throws IOException {
		Map<String, String> param = JSFUtils.getParam();
		
		// Pas de code autorisation => On renvoi vers la page de login
		String code = param.get("code");
		if( code == null )
			this.login(param.get("redirect_url"));
		
		// Si on a un code autorisation, on le traite
		else
			this.processAuthorizationCode(code, param.get("state"));		// dans state, on retrouve notre url de redirection initiale
		
		// Fin du traitement
		FacesContext.getCurrentInstance().responseComplete();
	}
	
	/**
	 * Retourne l'URL de redirection
	 * @return l'url de redirection
	 * @throws UnsupportedEncodingException 
	 */
	private String getEncodedRedirectUri() throws UnsupportedEncodingException {
		StringBuffer redirectUri = new StringBuffer();
		redirectUri.append(this.paramsBean.getBaseURI());
		if( !this.paramsBean.getBaseURI().endsWith("/") )
			redirectUri.append('/');
		redirectUri.append("init.xsp");
		return URLEncoder.encode(redirectUri.toString(), "UTF-8");
	}
	
	/**
	 * Redirige vers la page de logging
	 * @param redirectUri URL vers laquelle on veut revenir
	 * @throws UnsupportedEncodingException 
	 */
	private void login(String redirectUrl) throws UnsupportedEncodingException {
		// Envoi le redirect
		HttpServletResponse response = JSFUtils.getServletResponse();
		response.setStatus(302);
		response.setHeader(
				"Location", 
				this.paramsBean.getAuthorizeEndPoint() + "?" +
					"response_type=code&" +
					"redirect_uri=" + this.getEncodedRedirectUri() + "&" +
					"client_id=" + this.paramsBean.getClientId() + "&" +
					"state=" + URLEncoder.encode(redirectUrl, "UTF-8")
		);
	}
	
	/**
	 * Traite le code autorisation, et mémorise dans la session
	 * les deux tokens
	 * @param code le code autorisation
	 * @param redirectUrl l'url de redirection initiale
	 * @throws IOException 
	 */
	private void processAuthorizationCode(String code, String redirectUrl) throws IOException {
		StringBuffer authorizeUrl = new StringBuffer();
		authorizeUrl.append(this.paramsBean.getTokenEndPoint()).append('?');
		authorizeUrl.append("grant_type=authorization_code&");
		authorizeUrl.append("code=").append(code).append('&');
		authorizeUrl.append("client_id=").append(this.paramsBean.getClientId()).append('&');
		authorizeUrl.append("redirect_uri=").append(this.getEncodedRedirectUri());
		
		URL url = new URL(authorizeUrl.toString());
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		InputStream in = null;
		Reader reader = null;
		try {
			conn.setDoInput(true);
			conn.setDoOutput(false);
			conn.addRequestProperty("Authorization", "Basic " + this.paramsBean.getSecret());
			
			// Charge la réponse (du JSON)
			in = conn.getInputStream();
			reader = new InputStreamReader(in, "UTF-8");
			StringBuffer sb = new StringBuffer();
			char[] buff = new char[4 * 1024];
			int read = reader.read(buff);
			while( read != -1 ) {
				sb.append(buff, 0, read);
				read = reader.read(buff);
			}
			
			// Code 200 => OK, on a les tokens
			if( conn.getResponseCode() == 200 ) {
				GrantResponse grant = GsonUtils.fromJson(sb.toString(), GrantResponse.class);
				JSFUtils.getSessionScope().put("refresh_token", grant.getRefreshToken());
				JSFUtils.getSessionScope().put("access_token", grant.getAccessToken());
				
				HttpServletResponse response = JSFUtils.getServletResponse();
				response.setStatus(302);
				response.setHeader("Location", redirectUrl);
				
			// Code 400 => Erreur
			} if( conn.getResponseCode() == 400 ) {
				GrantError error = GsonUtils.fromJson(sb.toString(), GrantError.class);
				JSFUtils.getRequestScope().put("error", error);
			}
		} finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(in);
			conn.disconnect();
		}
	}
}
