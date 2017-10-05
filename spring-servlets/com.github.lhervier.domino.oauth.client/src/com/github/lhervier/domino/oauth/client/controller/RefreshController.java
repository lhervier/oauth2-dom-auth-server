package com.github.lhervier.domino.oauth.client.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import javax.servlet.http.HttpSession;

import lotus.domino.NotesException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.lhervier.domino.oauth.client.Constants;
import com.github.lhervier.domino.oauth.client.ex.OauthClientException;
import com.github.lhervier.domino.oauth.client.ex.RefreshTokenException;
import com.github.lhervier.domino.oauth.client.model.GrantError;
import com.github.lhervier.domino.oauth.client.model.GrantResponse;
import com.github.lhervier.domino.oauth.client.model.TokensResponse;
import com.github.lhervier.domino.oauth.client.utils.Callback;
import com.github.lhervier.domino.oauth.client.utils.Utils;
import com.github.lhervier.domino.oauth.client.utils.ValueHolder;
import com.github.lhervier.domino.spring.servlet.NotesContext;

@Controller
public class RefreshController {

	/**
	 * The http session
	 */
	@Autowired
	private HttpSession httpSession;
	
	/**
	 * The notes context
	 */
	@Autowired
	private NotesContext notesContext;
	
	/**
	 * The spring environment
	 */
	@Autowired
	private Environment env;
	
	/**
	 * The token controller
	 */
	@Autowired
	private TokenController tokenCtrl;
	
	/**
	 * Refresh the token
	 * @throws IOException 
	 * @throws NotesException 
	 * @throws UnsupportedEncodingException 
	 */
	@RequestMapping(value = "/refresh", method = RequestMethod.GET)
	public @ResponseBody TokensResponse refreshToken() throws OauthClientException, RefreshTokenException {
		try {
			String refreshToken = (String) this.httpSession.getAttribute(Constants.SESSION_REFRESH_TOKEN);
			
			// No refresh token => Unable to ask for a new one
			if( refreshToken == null )
				return this.tokenCtrl.tokens();
			
			final ValueHolder<RefreshTokenException> ex = new ValueHolder<RefreshTokenException>();
			Utils.createConnection(
					this.notesContext.getServerSession(), 
					Boolean.parseBoolean(this.env.getProperty("oauth2.client.disableHostVerifier")), 
					this.env.getProperty("oauth2.client.secret"),
					this.env.getProperty("oauth2.client.endpoints.token")
			)
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
					RefreshController.this.httpSession.setAttribute(Constants.SESSION_REFRESH_TOKEN, grant.getRefreshToken());
					RefreshController.this.httpSession.setAttribute(Constants.SESSION_ACCESS_TOKEN, grant.getAccessToken());
					RefreshController.this.httpSession.setAttribute(Constants.SESSION_ID_TOKEN, grant.getIdToken());
				}
			})
			
			// Erreur => Non autorisé
			.onError(new Callback<GrantError>() {
				@Override
				public void run(GrantError error) throws IOException {
					RefreshController.this.httpSession.setAttribute(Constants.SESSION_REFRESH_TOKEN, null);
					RefreshController.this.httpSession.setAttribute(Constants.SESSION_ACCESS_TOKEN, null);
					RefreshController.this.httpSession.setAttribute(Constants.SESSION_ID_TOKEN, null);
					ex.set(new RefreshTokenException(error));
				}
			})
			.execute();
			
			if( ex.get() != null )
				throw ex.get();
			
			return this.tokenCtrl.tokens();
		} catch(IOException e) {
			throw new OauthClientException(e);
		}
	}
}
