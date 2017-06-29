package com.github.lhervier.domino.oauth.library.client.bean;

import java.io.IOException;
import java.text.ParseException;

import com.github.lhervier.domino.oauth.common.HttpContext;
import com.github.lhervier.domino.oauth.common.NotesContext;
import com.github.lhervier.domino.oauth.common.model.error.GrantError;
import com.github.lhervier.domino.oauth.common.utils.Callback;
import com.github.lhervier.domino.oauth.common.utils.GsonUtils;
import com.github.lhervier.domino.oauth.library.client.model.GrantResponse;
import com.github.lhervier.domino.oauth.library.client.model.IdToken;
import com.github.lhervier.domino.oauth.library.client.utils.Utils;
import com.nimbusds.jose.JWSObject;

public class RefreshBean {

	/**
	 * La bean pour accéder aux paramètres
	 */
	private InitParamsBean initParamsBean;
	
	/**
	 * La bean pour accéder a l'access token
	 */
	private AccessTokenBean accessTokenBean;
	
	/**
	 * The notes context
	 */
	private NotesContext notesContext;
	
	/**
	 * The http context
	 */
	private HttpContext httpContext;
	
	/**
	 * Rafraîchit les tokens
	 * @throws IOException 
	 */
	public void refresh() throws IOException {
		String refreshToken = (String) this.httpContext.getSession().getAttribute("refresh_token");
		
		// Refresh token pas présent (session non initialisée ou expirée)
		if( refreshToken == null ) {
			this.accessTokenBean.sendToken();
			return;
		}
		
		Utils.createConnection(
				this.notesContext, 
				this.initParamsBean.isDisableHostNameVerifier(), 
				this.initParamsBean.getSecret(),
				this.initParamsBean.getTokenEndPoint())
				.setTextContent(
						new StringBuffer()
								.append("grant_type=refresh_token&")
								.append("refresh_token=").append(refreshToken)
								.toString(), 
						"UTF-8"
				)
				
				// OK => Met à jour la session et retourne le token
				.onOk(new Callback<GrantResponse>() {
					@Override
					public void run(GrantResponse grant) throws IOException, ParseException {
						RefreshBean.this.httpContext.getSession().setAttribute("refresh_token", grant.getRefreshToken());
						RefreshBean.this.httpContext.getSession().setAttribute("access_token", grant.getAccessToken());
						
						JWSObject jwsObj = JWSObject.parse(grant.getIdToken());
						String json = jwsObj.getPayload().toString();
						RefreshBean.this.httpContext.getSession().setAttribute("id_token", GsonUtils.fromJson(json, IdToken.class));
						
						RefreshBean.this.accessTokenBean.sendToken();
					}
				})
				
				// Erreur => Non autorisé
				.onError(new Callback<GrantError>() {
					@Override
					public void run(GrantError error) throws IOException {
						RefreshBean.this.httpContext.getSession().setAttribute("refresh_token", null);
						RefreshBean.this.httpContext.getSession().setAttribute("access_token", null);
						
						RefreshBean.this.accessTokenBean.sendToken();
					}
				})
				
				.execute();
	}
	
	// ======================================================================

	/**
	 * @param accessTokenBean the accessTokenBean to set
	 */
	public void setAccessTokenBean(AccessTokenBean accessTokenBean) {
		this.accessTokenBean = accessTokenBean;
	}

	/**
	 * @param initParamsBean the initParamsBean to set
	 */
	public void setInitParamsBean(InitParamsBean initParamsBean) {
		this.initParamsBean = initParamsBean;
	}

	/**
	 * @param notesContext the notesContext to set
	 */
	public void setNotesContext(NotesContext notesContext) {
		this.notesContext = notesContext;
	}

	public void setHttpContext(HttpContext httpContext) {
		this.httpContext = httpContext;
	}
}
