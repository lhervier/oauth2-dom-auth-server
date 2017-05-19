package com.github.lhervier.domino.oauth.library.client.bean;

import static com.github.lhervier.domino.oauth.common.utils.HttpUtils.createConnection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.faces.context.FacesContext;

import com.github.lhervier.domino.oauth.common.model.GrantResponse;
import com.github.lhervier.domino.oauth.common.model.error.GrantError;
import com.github.lhervier.domino.oauth.common.utils.Callback;
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
		
		// Si on a un code autorisation, on le traite
		if( param.containsKey("code") )
			this.processAuthorizationCode(code, param.get("state"));		// dans state, on retrouve notre url de redirection initiale
		
		// Si on a n'a pas d'erreur, on traite le login
		else if( !param.containsKey("error") )
			this.login(param.get("redirect_url"));
		
		// Sinon, on affiche l'erreur
		else
			return;
		
		// Fin du traitement
		FacesContext.getCurrentInstance().responseComplete();
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
	 * Traite le code autorisation, et mémorise dans la session
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
		
		createConnection(authorizeUrl.toString(), GrantResponse.class, GrantError.class)
				.addHeader("Authorization", "Basic " + this.paramsBean.getSecret())
				
				// OK => Mémorise les tokens en session et redirige vers l'url initiale
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
