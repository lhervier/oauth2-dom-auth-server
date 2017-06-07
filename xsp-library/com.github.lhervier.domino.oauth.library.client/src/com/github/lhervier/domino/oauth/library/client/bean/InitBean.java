package com.github.lhervier.domino.oauth.library.client.bean;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.faces.context.FacesContext;

import com.github.lhervier.domino.oauth.common.model.error.AuthorizeError;
import com.github.lhervier.domino.oauth.common.model.error.GrantError;
import com.github.lhervier.domino.oauth.common.utils.Callback;
import com.github.lhervier.domino.oauth.common.utils.JSFUtils;
import com.github.lhervier.domino.oauth.common.utils.QueryStringUtils;
import com.github.lhervier.domino.oauth.library.client.model.GrantResponse;
import com.github.lhervier.domino.oauth.library.client.utils.Utils;

public class InitBean {

	/**
	 * La bean pour accéder au paramétrage
	 */
	private InitParamsBean initParamsBean;
	
	/**
	 * Les paramétres de la requête
	 */
	private Map<String, String> param;
	
	/**
	 * Initialisation
	 * @throws IOException 
	 * @throws JsonDeserializeException 
	 */
	public void init() throws IOException {
		// Pas de code autorisation => On renvoi vers la page de login
		String code = this.param.get("code");
		
		// Si on a un code autorisation, on le traite
		if( this.param.containsKey("code") ) {
			this.processAuthorizationCode(code, this.param.get("state"));		// dans state, on retrouve notre url de redirection initiale
			FacesContext.getCurrentInstance().responseComplete();
		
		// Si on a une erreur, on l'affiche
		} else if( this.param.containsKey("error") ) {
			JSFUtils.getRequestScope().put(
					"error", 
					QueryStringUtils.createBean(JSFUtils.getParam(), AuthorizeError.class)
			);
		
		// Sinon, on traite le login
		} else {
			this.login(this.param.get("redirect_url"));
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
				this.initParamsBean.getAuthorizeEndPoint() + "?" +
					"response_type=code&" +
					"redirect_uri=" + Utils.getEncodedRedirectUri() + "&" +
					"client_id=" + this.initParamsBean.getClientId() + "&" +
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
		Utils.createConnection(this.initParamsBean.getTokenEndPoint())
				.setTextContent(
						new StringBuffer()
								.append("grant_type=authorization_code&")
								.append("code=").append(code).append('&')
								// .append("client_id=").append(this.initParamsBean.getClientId()).append('&')		// Facultatif
								.append("redirect_uri=").append(Utils.getEncodedRedirectUri())
								.toString(), 
						"UTF-8"
				)
				
				// OK => Mémorise les tokens en session et redirige vers l'url initiale
				.onOk(new Callback<GrantResponse>() {
					@Override
					public void run(GrantResponse grant) throws IOException {
						if( !"Bearer".equalsIgnoreCase(grant.getTokenType()) )
							throw new RuntimeException("Le seul type de token géré est Bearer... (et j'ai '"  + grant.getTokenType() + "')");
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
	
	// =================================================================================

	/**
	 * @param initParamsBean the initParamsBean to set
	 */
	public void setInitParamsBean(InitParamsBean initParamsBean) {
		this.initParamsBean = initParamsBean;
	}

	/**
	 * @param param the param to set
	 */
	public void setParam(Map<String, String> param) {
		this.param = param;
	}
}
