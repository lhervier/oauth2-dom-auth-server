package com.github.lhervier.domino.oauth.library.client.bean;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.faces.context.FacesContext;

import com.github.lhervier.domino.oauth.common.model.GrantResponse;
import com.github.lhervier.domino.oauth.common.model.error.AuthorizeError;
import com.github.lhervier.domino.oauth.common.model.error.GrantError;
import com.github.lhervier.domino.oauth.common.utils.Callback;
import com.github.lhervier.domino.oauth.common.utils.JSFUtils;
import com.github.lhervier.domino.oauth.common.utils.QueryStringUtils;
import com.github.lhervier.domino.oauth.library.client.utils.Utils;

public class InitBean {

	/**
	 * La bean pour acc�der au param�trage
	 */
	private InitParamsBean paramsBean;
	
	/**
	 * Constructeur
	 */
	public InitBean() {
		this.paramsBean = Utils.getInitParamsBean();
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
		
		// Si on a un code autorisation, on le traite
		if( param.containsKey("code") ) {
			this.processAuthorizationCode(code, param.get("state"));		// dans state, on retrouve notre url de redirection initiale
			FacesContext.getCurrentInstance().responseComplete();
		
		// Si on a une erreur, on l'affiche
		} else if( param.containsKey("error") ) {
			JSFUtils.getRequestScope().put(
					"error", 
					QueryStringUtils.createBean(JSFUtils.getParam(), AuthorizeError.class)
			);
		
		// Sinon, on traite le login
		} else {
			this.login(param.get("redirect_url"));
			FacesContext.getCurrentInstance().responseComplete();
		}
	}
	
	/**
	 * Redirige vers la page de logging
	 * @param redirectUri URL vers laquelle on veut revenir
	 * @throws UnsupportedEncodingException 
	 */
	private void login(String redirectUrl) throws UnsupportedEncodingException {
		JSFUtils.sendRedirect(
				this.paramsBean.getAuthorizeEndPoint() + "?" +
					"response_type=code&" +
					"redirect_uri=" + Utils.getEncodedRedirectUri() + "&" +
					"client_id=" + this.paramsBean.getClientId() + "&" +
					"state=" + URLEncoder.encode(redirectUrl, "UTF-8")
		);
	}
	
	/**
	 * Traite le code autorisation, et m�morise dans la session
	 * les deux tokens
	 * @param code le code autorisation
	 * @param redirectUrl l'url de redirection initiale
	 * @throws IOException 
	 */
	private void processAuthorizationCode(final String code, final String redirectUrl) throws IOException {
		StringBuffer authorizeUrl = new StringBuffer();
		authorizeUrl.append(this.paramsBean.getTokenEndPoint()).append('?');
		authorizeUrl.append("grant_type=authorization_code&");
		authorizeUrl.append("code=").append(code).append('&');
		authorizeUrl.append("client_id=").append(this.paramsBean.getClientId()).append('&');
		authorizeUrl.append("redirect_uri=").append(Utils.getEncodedRedirectUri());
		
		Utils.createConnection(authorizeUrl.toString())
				
				// OK => M�morise les tokens en session et redirige vers l'url initiale
				.onOk(new Callback<GrantResponse>() {
					@Override
					public void run(GrantResponse grant) throws IOException {
						JSFUtils.getSessionScope().put("refresh_token", grant.getRefreshToken());
						JSFUtils.getSessionScope().put("access_token", grant.getAccessToken());
						JSFUtils.sendRedirect(redirectUrl);
					}
				})
				
				// KO => Affiche l'erreur dans la XPage
				.onError(new Callback<GrantError>() {
					@Override
					public void run(GrantError error) throws IOException {
						JSFUtils.getRequestScope().put("error", error);
					}
				})
				
				.execute();
	}
}
