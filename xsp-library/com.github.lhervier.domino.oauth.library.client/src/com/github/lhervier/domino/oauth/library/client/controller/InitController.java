package com.github.lhervier.domino.oauth.library.client.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lotus.domino.NotesException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.github.lhervier.domino.oauth.common.model.error.AuthorizeError;
import com.github.lhervier.domino.oauth.common.model.error.GrantError;
import com.github.lhervier.domino.oauth.common.utils.Callback;
import com.github.lhervier.domino.oauth.common.utils.QueryStringUtils;
import com.github.lhervier.domino.oauth.library.client.BaseClientComponent;
import com.github.lhervier.domino.oauth.library.client.ex.OauthClientException;
import com.github.lhervier.domino.oauth.library.client.model.GrantResponse;
import com.github.lhervier.domino.oauth.library.client.utils.Utils;
import com.github.lhervier.domino.spring.servlet.NotesContext;

@Controller
public class InitController extends BaseClientComponent {

	/**
	 * The http servlet request
	 */
	@Autowired
	private HttpServletRequest request;
	
	/**
	 * The http session
	 */
	@Autowired
	private HttpSession session;
	
	/**
	 * The NotesContext
	 */
	@Autowired
	private NotesContext notesContext;
	
	/**
	 * Initialisation
	 * @throws IOException
	 */
	@RequestMapping(value = "/init")
	public ModelAndView init(
			@RequestParam(value = "code", required = false) String code,
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "redirect_url", required = false) String redirectUrl) throws OauthClientException {
		// Si on a un code autorisation, on le traite
		if( !StringUtils.isEmpty(code) ) {
			return this.processAuthorizationCode(code, state);		// dans state, on retrouve notre url de redirection initiale
		
		// Si on a une erreur, on l'affiche
		} else if( !StringUtils.isEmpty(error) ) {
			AuthorizeError authError = QueryStringUtils.createBean(
					this.request,
					AuthorizeError.class
			);
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("error", authError);
			return new ModelAndView("error", model);
		
		// Sinon, on redirige vers le endpoint authorize
		} else {
			// The authorize end point
			String authorizeEndPoint = this.getProperty("endpoints.authorize");
			String baseUri = Utils.getEncodedRedirectUri(this.getProperty("baseURI"));
			String clientId = this.getProperty("clientId");
			String encodedRedirectUrl = Utils.urlEncode(redirectUrl);
			String redirectUri = authorizeEndPoint + "?" +
						"response_type=code&" +
						"redirect_uri=" + baseUri + "&" +
						"client_id=" + clientId + "&" +
						"scope=openid profile email address phone&" +
						"state=" + encodedRedirectUrl;
			return new ModelAndView("redirect:" + redirectUri);
		}
	}
	
	/**
	 * Traite le code autorisation, et mémorise dans la session
	 * les deux tokens
	 * @param code le code autorisation
	 * @param redirectUrl l'url de redirection initiale
	 * @throws IOException 
	 * @throws NotesException 
	 */
	private ModelAndView processAuthorizationCode(final String code, final String redirectUrl) throws OauthClientException {
		try {
			final ModelAndView ret = new ModelAndView();
			Utils.createConnection(
					this.notesContext, 
					Boolean.parseBoolean(this.getProperty("disableHostVerifier")), 
					this.getProperty("secret"),
					this.getProperty("endpoints.token"))
					.setTextContent(
							new StringBuffer()
									.append("grant_type=authorization_code&")
									.append("code=").append(code).append('&')
									// .append("client_id=").append(this.initParamsBean.getClientId()).append('&')		// Not mandatory
									.append("redirect_uri=").append(Utils.getEncodedRedirectUri(this.getProperty("baseURI")))
									.toString(), 
							"UTF-8"
					)
					
					// OK => Mémorise les tokens en session et redirige vers l'url initiale
					.onOk(new Callback<GrantResponse>() {
						@Override
						public void run(GrantResponse grant) throws IOException, ParseException {
							if( !"Bearer".equalsIgnoreCase(grant.getTokenType()) )
								throw new RuntimeException("Le seul type de token géré est 'Bearer'... (et j'ai '"  + grant.getTokenType() + "')");
							InitController.this.session.setAttribute("ACCESS_TOKEN", grant.getAccessToken());
							InitController.this.session.setAttribute("REFRESH_TOKEN", grant.getRefreshToken());
							InitController.this.session.setAttribute("ID_TOKEN", grant.getIdToken());
							ret.setViewName("redirect:" + redirectUrl);
						}
					})
					
					// KO => Affiche l'erreur dans la XPage
					.onError(new Callback<GrantError>() {
						@Override
						public void run(GrantError error) throws IOException {
							ret.addObject("error", error);
							ret.setViewName("error");
						}
					})
					
					.execute();
			return ret;
		} catch(IOException e) {
			throw new OauthClientException(e);
		}
	}
}
