package com.github.lhervier.domino.oauth.library.client.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import javax.servlet.http.HttpSession;

import lotus.domino.NotesException;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.lhervier.domino.oauth.common.model.error.GrantError;
import com.github.lhervier.domino.oauth.common.utils.Callback;
import com.github.lhervier.domino.oauth.library.client.BaseClientComponent;
import com.github.lhervier.domino.oauth.library.client.Constants;
import com.github.lhervier.domino.oauth.library.client.ex.OauthClientException;
import com.github.lhervier.domino.oauth.library.client.model.GrantResponse;
import com.github.lhervier.domino.oauth.library.client.utils.Utils;
import com.github.lhervier.domino.spring.servlet.NotesContext;

@Controller
public class TokenController extends BaseClientComponent {

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
	 * The response class
	 */
	public static class TokenResponse {
		@JsonProperty("access_token")
		private String accessToken;
		@JsonProperty("id_token")
		private String idToken;
		public String getAccessToken() {return accessToken;}
		public void setAccessToken(String accessToken) {this.accessToken = accessToken;}
		public String getIdToken() {return idToken;}
		public void setIdToken(String idToken) {this.idToken = idToken;}
	}
	
	/**
	 * Send the access token
	 */
	@RequestMapping(value = "/accesstoken", method = RequestMethod.GET)
	public @ResponseBody TokenResponse accessToken() {
		TokenResponse resp = new TokenResponse();
		resp.setAccessToken((String) this.httpSession.getAttribute(Constants.SESSION_ACCESS_TOKEN));
		resp.setIdToken((String) this.httpSession.getAttribute(Constants.SESSION_ID_TOKEN));
		return resp;
	}
	
	/**
	 * Refresh the token
	 * @throws IOException 
	 * @throws NotesException 
	 * @throws UnsupportedEncodingException 
	 */
	@RequestMapping(value = "/refresh", method = RequestMethod.GET)
	public @ResponseBody TokenResponse refreshToken() throws OauthClientException {
		try {
			String refreshToken = (String) this.httpSession.getAttribute(Constants.SESSION_REFRESH_TOKEN);
			
			// Refresh token pas présent (session non initialisée ou expirée)
			if( refreshToken == null )
				return this.accessToken();
			
			Utils.createConnection(
					this.notesContext, 
					Boolean.parseBoolean(this.getProperty("disableHostVerifier")), 
					this.getProperty("secret"),
					this.getProperty("endpoints.token"))
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
							TokenController.this.httpSession.setAttribute(Constants.SESSION_REFRESH_TOKEN, grant.getRefreshToken());
							TokenController.this.httpSession.setAttribute(Constants.SESSION_ACCESS_TOKEN, grant.getAccessToken());
							TokenController.this.httpSession.setAttribute(Constants.SESSION_ID_TOKEN, grant.getIdToken());
						}
					})
					
					// Erreur => Non autorisé
					.onError(new Callback<GrantError>() {
						@Override
						public void run(GrantError error) throws IOException {
							TokenController.this.httpSession.setAttribute(Constants.SESSION_REFRESH_TOKEN, null);
							TokenController.this.httpSession.setAttribute(Constants.SESSION_ACCESS_TOKEN, null);
							TokenController.this.httpSession.setAttribute(Constants.SESSION_ID_TOKEN, null);
						}
					})
					
					.execute();
			return this.accessToken();
		} catch(IOException e) {
			throw new OauthClientException(e);
		}
	}
}
