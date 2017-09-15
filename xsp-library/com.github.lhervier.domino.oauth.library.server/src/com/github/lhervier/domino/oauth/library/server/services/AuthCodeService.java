package com.github.lhervier.domino.oauth.library.server.services;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.View;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.github.lhervier.domino.oauth.common.utils.SystemUtils;
import com.github.lhervier.domino.oauth.library.server.ex.grant.InvalidGrantException;
import com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.library.server.ext.IScopeGranter;
import com.github.lhervier.domino.oauth.library.server.model.Application;
import com.github.lhervier.domino.oauth.library.server.model.AuthorizationCode;
import com.github.lhervier.domino.oauth.library.server.utils.Utils;
import com.github.lhervier.domino.spring.servlet.NotesContext;
import com.google.gson.JsonObject;

/**
 * Service to manage authorization codes
 * @author Lionel HERVIER
 */
@Service
public class AuthCodeService {

	/**
	 * Securely generate random numbers
	 */
	private static final SecureRandom RANDOM = new SecureRandom();
	
	/**
	 * Le nom de la vue qui contient les codes autorization
	 */
	private static final String VIEW_AUTHCODES = "AuthorizationCodes";
	
	/**
	 * The notes context
	 */
	@Autowired
	private NotesContext notesContext;
	
	/**
	 * The application context
	 */
	@Autowired
	private ApplicationContext springContext;
	
	/**
	 * The http servlet request
	 */
	@Autowired
	private HttpServletRequest request;
	
	/**
	 * The application root
	 */
	@Value("${oauth2.server.applicationRoot}")
	private String applicationRoot;
	
	/**
	 * Authorization codes life time
	 */
	@Value("${oauth2.server.authCodeLifeTime}")
	private long authCodeLifeTime;
	
	/**
	 * The database where to store application information
	 */
	@Value("${oauth2.server.db}")
	private String oauth2db;
	
	/**
	 * Return the oauth2 database as the server
	 * @throws NotesException 
	 */
	private Database getOauth2DatabaseAsServer() throws NotesException {
		return DominoUtils.openDatabase(this.notesContext.getServerSession(), this.oauth2db);
	}
	
	/**
	 * Return the oauth2 database as the user (the application)
	 * @throws NotesException 
	 */
	private Database getOauth2DatabaseAsUser() throws NotesException {
		return DominoUtils.openDatabase(this.notesContext.getUserSession(), this.oauth2db);
	}
	
	/**
	 * Create an random id
	 * @return l'identifiant
	 */
	private String generateCode() {
		// see https://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
		return new BigInteger(260, RANDOM).toString(32);
	}
	
	/**
	 * Add an authorization code
	 * @param app the application to generate a token for
	 * @param redirectUri the redirect Uri
	 * @param scopes the scopes
	 * @throws NotesException 
	 */
	public AuthorizationCode createAuthorizationCode(
			Application app, 
			String redirectUri, 
			List<String> scopes) throws NotesException {
		Name nn = null;
		Document authDoc = null;
		try {
			nn = this.notesContext.getUserSession().createName(app.getName() + this.applicationRoot);
			String id = this.generateCode();
			
			AuthorizationCode authCode = new AuthorizationCode();
			authCode.setId(id);
			authCode.setApplication(nn.toString());
			authCode.setClientId(app.getClientId());
			authCode.setRedirectUri(redirectUri);
			authCode.setExpires(SystemUtils.currentTimeSeconds() + this.authCodeLifeTime);
			
			// Define scopes
			authCode.setScopes(scopes);
			
			// Update authorized scopes
			final List<String> grantedScopes = new ArrayList<String>();
			JsonObject contexts = new JsonObject();
			
			Map<String, IOAuthExtension> exts = this.springContext.getBeansOfType(IOAuthExtension.class);
			for( IOAuthExtension ext : exts.values() ) {
				JsonObject context = ext.authorize(
						new IScopeGranter() {
							@Override
							public void grant(String scope) {
								grantedScopes.add(scope);
							}
						}, 
						app.getClientId(), 
						scopes
				);
				if( context != null )
					contexts.add(ext.getId(), context);
			}
			
			authCode.setGrantedScopes(grantedScopes);
			authCode.setContexts(contexts);
			
			// On le persiste dans la base
			authDoc = Utils.getOauth2Database(this.request, this.notesContext, this.oauth2db).createDocument();
			authDoc.replaceItemValue("Form", "AuthorizationCode");
			DominoUtils.fillDocument(authDoc, authCode);
			
			DominoUtils.computeAndSave(authDoc);
			
			return authCode;
		} finally {
			DominoUtils.recycleQuietly(nn);
			DominoUtils.recycleQuietly(authDoc);
		}
	}
	
	/**
	 * Remove an authorization code
	 * @param code the auth code
	 */
	public void removeAuthorizationCode(String code) throws NotesException {
		View v = null;
		Document authDoc = null;
		try {
			// User (application) does not have the rights to remove document from the database
			v = DominoUtils.getView(this.getOauth2DatabaseAsServer(), VIEW_AUTHCODES);
			authDoc = v.getDocumentByKey(code, true);
			if( authDoc == null )
				return;
			if( !authDoc.remove(true) )
				throw new RuntimeException("Unable to remove auth code document !");
		} finally {
			DominoUtils.recycleQuietly(authDoc);
			DominoUtils.recycleQuietly(v);
		}
	}
	
	/**
	 * Get an authorization code
	 * @param code the code
	 * @return the authorization code (or null if it does not exists)
	 */
	public AuthorizationCode getAuthorizationCode(String code) throws InvalidGrantException, NotesException {
		Document authDoc = null;
		View v = null;
		try {
			v = this.getOauth2DatabaseAsUser().getView(VIEW_AUTHCODES);
			authDoc = v.getDocumentByKey(code, true);
			if( authDoc == null )
				throw new InvalidGrantException();
			return DominoUtils.fillObject(new AuthorizationCode(), authDoc);
		} finally {
			DominoUtils.recycleQuietly(authDoc);
			DominoUtils.recycleQuietly(v);
		}
	}
	
}
