package com.github.lhervier.domino.oauth.library.client.bean;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;

import org.apache.commons.lang.StringUtils;

import com.github.lhervier.domino.oauth.common.HttpContext;
import com.github.lhervier.domino.oauth.common.NotesContext;
import com.github.lhervier.domino.oauth.common.model.error.AuthorizeError;
import com.github.lhervier.domino.oauth.common.model.error.GrantError;
import com.github.lhervier.domino.oauth.common.utils.Callback;
import com.github.lhervier.domino.oauth.common.utils.GsonUtils;
import com.github.lhervier.domino.oauth.common.utils.JSFUtils;
import com.github.lhervier.domino.oauth.common.utils.QueryStringUtils;
import com.github.lhervier.domino.oauth.common.utils.ValueHolder;
import com.github.lhervier.domino.oauth.library.client.ex.InitException;
import com.github.lhervier.domino.oauth.library.client.model.GrantResponse;
import com.github.lhervier.domino.oauth.library.client.model.IdToken;
import com.github.lhervier.domino.oauth.library.client.model.InitResponse;
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
	 * @return the grant response
	 * @throws InitException
	 */
	public InitResponse init() throws InitException {
		try {
			// Pas de code autorisation => On renvoi vers la page de login
			String code = this.httpContext.getRequest().getParameter("code");
			
			// Si on a un code autorisation, on le traite
			if( !StringUtils.isEmpty(this.httpContext.getRequest().getParameter("code")) ) {
				return this.processAuthorizationCode(code);		// dans state, on retrouve notre url de redirection initiale
			
			// Si on a une erreur, on l'affiche
			} else if( !StringUtils.isEmpty(this.httpContext.getRequest().getParameter("error")) ) {
				throw new InitException(QueryStringUtils.createBean(
						this.httpContext.getRequest(), 
						AuthorizeError.class
				));
			
			// Sinon, on traite le login
			} else {
				JSFUtils.sendRedirect(
						this.httpContext.getResponse(),
						this.initParamsBean.getAuthorizeEndPoint() + "?" +
							"response_type=code&" +
							"redirect_uri=" + Utils.getEncodedRedirectUri() + "&" +
							"client_id=" + this.initParamsBean.getClientId() + "&" +
							"scope=openid profile email address phone&" +
							"state=" + URLEncoder.encode(this.httpContext.getRequest().getParameter("redirect_url"), "UTF-8")
				);
				return null;
			}
		} catch(IOException e) {
			throw new InitException(e.getMessage(), e);
		}
	}
	
	/**
	 * Traite le code autorisation, et mémorise dans la session
	 * les deux tokens
	 * @param code le code autorisation
	 * @param redirectUrl l'url de redirection initiale
	 * @throws IOException 
	 * @throws InitException
	 */
	private InitResponse processAuthorizationCode(final String code) throws IOException, InitException {
		final ValueHolder<InitException> ex = new ValueHolder<InitException>();
		final ValueHolder<InitResponse> resp = new ValueHolder<InitResponse>();
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
						resp.set(new InitResponse());
						
						resp.get().setRefreshToken(grant.getRefreshToken());
						resp.get().setAccessToken(grant.getAccessToken());
						
						// Décode le id_token openid
						JWSObject jwsObj = JWSObject.parse(grant.getIdToken());
						String json = jwsObj.getPayload().toString();
						
						resp.get().setIdToken(GsonUtils.fromJson(json, IdToken.class));
					}
				})
				
				// KO => Affiche l'erreur dans la XPage
				.onError(new Callback<GrantError>() {
					@Override
					public void run(GrantError error) throws IOException {
						ex.set(new InitException(error));
					}
				})
				
				.execute();
		if( ex.get() != null )
			throw ex.get();
		return resp.get();
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
