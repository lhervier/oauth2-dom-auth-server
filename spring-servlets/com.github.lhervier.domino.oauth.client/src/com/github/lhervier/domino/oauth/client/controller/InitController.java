package com.github.lhervier.domino.oauth.client.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lotus.domino.NotesException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.github.lhervier.domino.oauth.client.ex.OauthClientException;
import com.github.lhervier.domino.oauth.client.model.AuthorizeError;
import com.github.lhervier.domino.oauth.client.model.GrantError;
import com.github.lhervier.domino.oauth.client.model.GrantResponse;
import com.github.lhervier.domino.oauth.client.utils.Callback;
import com.github.lhervier.domino.oauth.client.utils.QueryStringUtils;
import com.github.lhervier.domino.oauth.client.utils.StringUtils;
import com.github.lhervier.domino.oauth.client.utils.Utils;
import com.github.lhervier.domino.spring.servlet.NotesContext;

@Controller
public class InitController {

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
	 * The notes context
	 */
	@Autowired
	private NotesContext notesContext;
	
	/**
	 * Spring environment
	 */
	@Autowired
	private Environment env;
	
	/**
	 * Initialisation
	 * @throws OauthClientException
	 */
	@RequestMapping(value = "/init")
	public ModelAndView init(
			@RequestParam(value = "code", required = false) String code,
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "redirect_url", required = false) String redirectUrl) throws OauthClientException {
		// If we have a authorization code, we can process it
		if( !StringUtils.isEmpty(code) )
			return this.processAuthorizationCode(code, state);		// dans state, on retrouve notre url de redirection initiale
		
		// If we have an error, we display it
		if( !StringUtils.isEmpty(error) ) {
			AuthorizeError authError = QueryStringUtils.createBean(
					this.request,
					AuthorizeError.class
			);
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("error", authError);
			return new ModelAndView("authorizeError", model);
		}
		
		// Otherwise, we redirect to the authorize endpoint
		String authorizeEndPoint = this.env.getProperty("oauth2.client.endpoints.authorize");
		String baseUri = Utils.getEncodedRedirectUri(this.env.getProperty("oauth2.client.baseURI"));
		String clientId = this.env.getProperty("oauth2.client.clientId");
		String encodedRedirectUrl = Utils.urlEncode(redirectUrl);
		String redirectUri = authorizeEndPoint + "?" +
					"response_type=code+id_token&" +
					"redirect_uri=" + baseUri + "&" +
					"client_id=" + clientId + "&" +
					"scope=openid profile email address phone&" +
					"state=" + encodedRedirectUrl;
		return new ModelAndView("redirect:" + redirectUri);
	}
	
	/**
	 * Process authorization code, and fill session with tokens.
	 * @param code le code autorisation
	 * @param redirectUrl l'url de redirection initiale
	 * @throws IOException 
	 * @throws NotesException 
	 */
	private ModelAndView processAuthorizationCode(final String code, final String redirectUrl) throws OauthClientException {
		try {
			final ModelAndView ret = new ModelAndView();
			Utils.createConnection(
					this.notesContext.getServerSession(), 
					Boolean.parseBoolean(this.env.getProperty("oauth2.client.disableHostVerifier")), 
					this.env.getProperty("oauth2.client.secret"),
					this.env.getProperty("oauth2.client.endpoints.token"))
					.setTextContent(
							new StringBuffer()
									.append("grant_type=authorization_code&")
									.append("code=").append(code).append('&')
									// .append("client_id=").append(this.initParamsBean.getClientId()).append('&')		// Not mandatory
									.append("redirect_uri=").append(Utils.getEncodedRedirectUri(this.env.getProperty("oauth2.client.baseURI")))
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
							ret.setViewName("grantError");
						}
					})
					
					.execute();
			return ret;
		} catch(IOException e) {
			throw new OauthClientException(e);
		}
	}
}
