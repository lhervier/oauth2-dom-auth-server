package com.github.lhervier.domino.oauth.server.controller;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.http.HttpServletResponse;

import lotus.domino.NotesException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.github.lhervier.domino.oauth.server.aop.ann.Oauth2DbContext;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ext.core.AccessToken;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.services.AppService;
import com.github.lhervier.domino.oauth.server.services.SecretService;
import com.github.lhervier.domino.oauth.server.utils.SystemUtils;
import com.github.lhervier.domino.spring.servlet.NotesContext;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;

/**
 * Token introspection endpoint;
 * See https://tools.ietf.org/html/rfc7662
 * @author Lionel HERVIER
 */
@Controller
public class CheckTokenController {

	/**
	 * Logger
	 */
	private static final Log LOG = LogFactory.getLog(CheckTokenController.class);
	
	/**
	 * Secret service
	 */
	@Autowired
	private SecretService secretSvc;
	
	/**
	 * App service
	 */
	@Autowired
	private AppService appService;
	
	/**
	 * Notes context
	 */
	@Autowired
	private NotesContext notesCtx;
	
	/**
	 * SSO config used to sign access tokens
	 */
	@Value("${oauth2.server.core.signKey}")
	private String signKey;
	
	/**
	 * Jackson mapper
	 */
	private ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * For CORS requests
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "/checkToken", method = RequestMethod.OPTIONS)
    @ResponseStatus(HttpStatus.OK)
	@Oauth2DbContext
	public void handleCors(HttpServletResponse response) throws IOException {
        response.addHeader("Access-Control-Allow-Headers", "authorization");
        response.addHeader("Access-Control-Max-Age", "60"); // seconds to cache preflight request --> less OPTIONS traffic
        response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.addHeader("Access-Control-Allow-Origin", "*");
    }
	
	/**
	 * Token introspection response
	 */
	public static class CheckTokenResponse {
		private boolean active;
		private String scope;
		@JsonProperty("client_id")
		private String clientId;
		private String username;
		@JsonProperty("user_name")
		private String springUsername;
		@JsonProperty("token_type")
		private String tokenType;
		private long exp;
		private long iat;
		private long nbf;
		private String sub;
		private String aud;
		private String iss;
		public boolean isActive() { return active; }
		public void setActive(boolean active) { this.active = active; }
		public String getScope() { return scope; }
		public void setScope(String scope) { this.scope = scope; }
		public String getClientId() { return clientId; }
		public void setClientId(String clientId) { this.clientId = clientId; }
		public String getUsername() { return username; }
		public void setUsername(String username) { this.username = username; }
		public String getTokenType() { return tokenType; }
		public void setTokenType(String tokenType) { this.tokenType = tokenType; }
		public long getExp() { return exp; }
		public void setExp(long exp) { this.exp = exp; }
		public long getIat() { return iat; }
		public void setIat(long iat) { this.iat = iat; }
		public long getNbf() { return nbf; }
		public void setNbf(long nbf) { this.nbf = nbf; }
		public String getSpringUsername() { return springUsername; }
		public void setSpringUsername(String springUsername) { this.springUsername = springUsername;}
		public String getSub() { return sub; }
		public void setSub(String sub) { this.sub = sub; }
		public String getAud() { return aud; }
		public void setAud(String aud) { this.aud = aud; }
		public String getIss() { return iss; }
		public void setIss(String iss) { this.iss = iss; }
	}
	
	@RequestMapping(value = "/checkToken", method = RequestMethod.POST)
	@Oauth2DbContext
	public @ResponseBody CheckTokenResponse checkToken(
			@RequestParam("token") String token) throws IOException, NotesException, NotAuthorizedException {
		// User must be logged in as an application
		Application userApp = this.appService.getApplicationFromFullName(this.notesCtx.getUserSession().getEffectiveUserName());
		if( userApp == null ) {
			LOG.error("Not logged in as an application");
			throw new NotAuthorizedException();
		}
		
		// Parse the JWT
		JWSObject jwsObj;
		try {
			jwsObj = JWSObject.parse(token);
		} catch (ParseException e) {
			throw new NotAuthorizedException();
		}
		
		// Check sign key
		String kid = jwsObj.getHeader().getKeyID();
		if( !this.signKey.equals(kid) ) {
			LOG.error("kid incorrect in Bearer token");
			throw new NotAuthorizedException();
		}
		
		// Check algorithm
		String alg = jwsObj.getHeader().getAlgorithm().getName();
		if( !"HS256".equals(alg) ) {
			LOG.error("alg incorrect in Bearer token");
			throw new NotAuthorizedException();
		}
		
		// Check signature
		byte[] secret = this.secretSvc.getSignSecret(kid);
		try {
			JWSVerifier verifier = new MACVerifier(secret);
			if( !jwsObj.verify(verifier) ) {
				LOG.error("Bearer token verification failed");
				throw new NotAuthorizedException();
			}
		} catch (JOSEException e) {
			LOG.error("Error verifying token");
			throw new NotAuthorizedException();
		}
		
		// Deserialize the token
		AccessToken tk = this.mapper.readValue(jwsObj.getPayload().toString(), AccessToken.class);
		
		// Mark active/inactive
		CheckTokenResponse resp = new CheckTokenResponse();
		resp.setActive(tk.getExp() < SystemUtils.currentTimeSeconds());
		resp.setClientId(tk.getAud());
		resp.setExp(tk.getExp());
		resp.setTokenType("Bearer");
		resp.setUsername(tk.getSub());
		resp.setSpringUsername(resp.getUsername());			// Spring OAUTH2 Security will look at the "user_name" property insted of the "username" property (as defined in RFC7662)
		resp.setScope(StringUtils.join(tk.getScopes().iterator(), ' '));
		resp.setSub(tk.getSub());
		resp.setIss(tk.getIss());
		// resp.setAud(tk.getAud());
		return resp;
	}
}
