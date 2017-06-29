package com.github.lhervier.domino.oauth.library.client.bean;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;

import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;

import com.github.lhervier.domino.oauth.common.HttpContext;
import com.github.lhervier.domino.oauth.common.NotesContext;
import com.github.lhervier.domino.oauth.common.model.error.AuthorizeError;
import com.github.lhervier.domino.oauth.common.model.error.GrantError;
import com.github.lhervier.domino.oauth.common.utils.Callback;
import com.github.lhervier.domino.oauth.common.utils.GsonUtils;
import com.github.lhervier.domino.oauth.common.utils.JSFUtils;
import com.github.lhervier.domino.oauth.common.utils.QueryStringUtils;
import com.github.lhervier.domino.oauth.library.client.model.GrantResponse;
import com.github.lhervier.domino.oauth.library.client.model.IdToken;
import com.github.lhervier.domino.oauth.library.client.utils.Utils;
import com.nimbusds.jose.JWSObject;

public class InitBean {

	/**
	 * La bean pour accéder au paramétrage
	 */
	private InitParamsBean initParamsBean;
	
	/**
	 * The notes context
	 */
	private NotesContext notesContext;
	
	/**
	 * The http context
	 */
	private HttpContext httpContext;
	
	/**
	 * Initialisation
	 * @throws IOException 
	 * @throws JsonDeserializeException 
	 */
	public void init() throws IOException {
		// Pas de code autorisation => On renvoi vers la page de login
		String code = this.httpContext.getRequest().getParameter("code");
		
		// Si on a un code autorisation, on le traite
		if( !StringUtils.isEmpty(this.httpContext.getRequest().getParameter("code")) ) {
			this.processAuthorizationCode(code, this.httpContext.getRequest().getParameter("state"));		// dans state, on retrouve notre url de redirection initiale
			FacesContext.getCurrentInstance().responseComplete();
		
		// Si on a une erreur, on l'affiche
		} else if( !StringUtils.isEmpty(this.httpContext.getRequest().getParameter("error")) ) {
			JSFUtils.getRequestScope().put(
					"error", 
					QueryStringUtils.createBean(this.httpContext.getRequest(), AuthorizeError.class)
			);
		
		// Sinon, on traite le login
		} else {
			this.login(this.httpContext.getRequest().getParameter("redirect_url"));
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
				this.httpContext.getResponse(),
				this.initParamsBean.getAuthorizeEndPoint() + "?" +
					"response_type=code&" +
					"redirect_uri=" + Utils.getEncodedRedirectUri() + "&" +
					"client_id=" + this.initParamsBean.getClientId() + "&" +
					"scope=openid profile email address phone&" +
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
		Utils.createConnection(this.notesContext, this.initParamsBean.getTokenEndPoint())
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
					public void run(GrantResponse grant) throws IOException, ParseException {
						if( !"Bearer".equalsIgnoreCase(grant.getTokenType()) )
							throw new RuntimeException("Le seul type de token géré est Bearer... (et j'ai '"  + grant.getTokenType() + "')");
						JSFUtils.getSessionScope().put("refresh_token", grant.getRefreshToken());
						JSFUtils.getSessionScope().put("access_token", grant.getAccessToken());
						
						// Décode le id_token openid
						JWSObject jwsObj = JWSObject.parse(grant.getIdToken());
						// JWSVerifier verifier = new MACVerifier(this.getSecret());		// FIXME: Il faut vérifier le JWS
						// if( jwsObj.verify(verifier) ) {
							// Extrait le contenu du token
							String json = jwsObj.getPayload().toString();
							JSFUtils.getSessionScope().put("id_token", GsonUtils.fromJson(json, IdToken.class));
						// }
						JSFUtils.sendRedirect(InitBean.this.httpContext.getResponse(), redirectUrl);
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
	 * @param notesContext the notesContext to set
	 */
	public void setNotesContext(NotesContext notesContext) {
		this.notesContext = notesContext;
	}

	/**
	 * @param httpContext the httpContext to set
	 */
	public void setHttpContext(HttpContext httpContext) {
		this.httpContext = httpContext;
	}
}
