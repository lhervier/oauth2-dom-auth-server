package com.github.lhervier.domino.oauth.server.services;

import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ext.core.AccessToken;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.model.TokenContent;
import com.github.lhervier.domino.oauth.server.repo.SecretRepository;
import com.github.lhervier.domino.oauth.server.utils.SystemUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;

/**
 * Service to check for tokens
 * @author Lionel HERVIER
 */
@Service
public class CheckTokenService {

	/**
	 * Logger
	 */
	private static final Log LOG = LogFactory.getLog(CheckTokenService.class);
	
	/**
	 * Secret repository
	 */
	@Autowired
	private SecretRepository secretRepo;
	
	/**
	 * App service
	 */
	@Autowired
	private AppService appService;
	
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
	 * Check if the token is OK
	 * @param user the current logged in user
	 * @param token the token to check
	 * @return the token
	 * @throws IOException
	 * @throws NotAuthorizedException
	 */
	public TokenContent checkToken(
			NotesPrincipal user, 
			String token) throws NotAuthorizedException {
		// User must be logged in as an application
		Application userApp = this.appService.getApplicationFromName(user.getCommon());
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
		byte[] secret = this.secretRepo.findSignSecret(kid);
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
		AccessToken tk;
		try {
			tk = this.mapper.readValue(jwsObj.getPayload().toString(), AccessToken.class);
		} catch (JsonParseException e) {
			throw new NotAuthorizedException();
		} catch (JsonMappingException e) {
			throw new NotAuthorizedException();
		} catch (IOException e) {
			throw new NotAuthorizedException();
		}
		
		// Mark active/inactive
		TokenContent resp = new TokenContent();
		resp.setActive(tk.getExp() > SystemUtils.currentTimeSeconds());
		if( resp.isActive() ) {
			resp.setClientId(tk.getAud());
			resp.setExp(tk.getExp());
			resp.setTokenType("Bearer");
			resp.setUsername(tk.getSub());
			resp.setSpringUsername(resp.getUsername());			// Spring OAUTH2 Security will look at the "user_name" property insted of the "username" property (as defined in RFC7662)
			resp.setScope(StringUtils.join(tk.getScopes().iterator(), ' '));
			resp.setSub(tk.getSub());
			resp.setIss(tk.getIss());
			// resp.setAud(tk.getAud());						// OPTIONAL. Will make Spring Security calls fail...
		}
		return resp;
	}
}
