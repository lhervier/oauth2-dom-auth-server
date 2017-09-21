package com.github.lhervier.domino.oauth.library.server.services;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import lotus.domino.Document;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.github.lhervier.domino.oauth.common.utils.SystemUtils;
import com.github.lhervier.domino.oauth.library.server.BaseServerComponent;
import com.github.lhervier.domino.oauth.library.server.ex.grant.InvalidGrantException;
import com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.library.server.ext.IScopeGranter;
import com.github.lhervier.domino.oauth.library.server.model.Application;
import com.github.lhervier.domino.oauth.library.server.model.AuthorizationCode;

/**
 * Service to manage authorization codes
 * @author Lionel HERVIER
 */
@Service
public class AuthCodeService extends BaseServerComponent {

	/**
	 * Securely generate random numbers
	 */
	private static final SecureRandom RANDOM = new SecureRandom();
	
	/**
	 * Le nom de la vue qui contient les codes autorization
	 */
	private static final String VIEW_AUTHCODES = "AuthorizationCodes";
	
	/**
	 * The application context
	 */
	@Autowired
	private ApplicationContext springContext;
	
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
	 * Jackson object mapper
	 */
	private ObjectMapper mapper = new ObjectMapper();
	
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
	 * @param session the session (as the user)
	 * @param app the application to generate a token for
	 * @param redirectUri the redirect Uri
	 * @param scopes the scopes
	 * @throws NotesException 
	 */
	public AuthorizationCode createAuthorizationCode(
			Session session,
			Application app, 
			String redirectUri, 
			List<String> scopes) throws NotesException {
		// Get the app name
		Name nn = null;
		String appName;
		try {
			nn = session.createName(app.getName() + this.applicationRoot);
			appName = nn.toString();
		} finally {
			DominoUtils.recycleQuietly(nn);
		}
		
		try {
			String id = this.generateCode();
			
			AuthorizationCode authCode = new AuthorizationCode();
			authCode.setId(id);
			authCode.setApplication(appName);
			authCode.setClientId(app.getClientId());
			authCode.setRedirectUri(redirectUri);
			authCode.setExpires(SystemUtils.currentTimeSeconds() + this.authCodeLifeTime);
			authCode.setContextClasses(new HashMap<String, String>());
			authCode.setContextObjects(new HashMap<String, String>());
			
			// Define scopes
			authCode.setScopes(scopes);
			
			// Update authorized scopes
			final List<String> grantedScopes = new ArrayList<String>();
			Map<String, IOAuthExtension> exts = this.springContext.getBeansOfType(IOAuthExtension.class);
			for( IOAuthExtension ext : exts.values() ) {
				Object context = ext.authorize(
						session,
						new IScopeGranter() {
							@Override
							public void grant(String scope) {
								grantedScopes.add(scope);
							}
						}, 
						app.getClientId(), 
						scopes
				);
				if( context != null ) {
					authCode.getContextObjects().put(ext.getId(), this.mapper.writeValueAsString(context));
					authCode.getContextClasses().put(ext.getId(), ext.getContextClass().getName());
				}
			}
			authCode.setGrantedScopes(grantedScopes);
			
			// Persist the authorization code
			this.saveAuthCode(authCode);
			
			return authCode;
		} catch (JsonGenerationException e) {
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Save an authorization code
	 * @param authCode the authorization code to save
	 */
	public void saveAuthCode(AuthorizationCode authCode) throws NotesException {
		Document authDoc = null;
		try {
			authDoc = this.getOauth2DatabaseAsServer().createDocument();
			authDoc.replaceItemValue("Form", "AuthorizationCode");
			DominoUtils.fillDocument(authDoc, authCode);		// Will not persist Map properties
			Vector<String> extIds = new Vector<String>();		// Domino need Vector...
			for( String extId : authCode.getContextClasses().keySet() ) {
				authDoc.replaceItemValue(
						"Context_Class_" + extId, 
						authCode.getContextClasses().get(extId)
				);
				authDoc.replaceItemValue(
						"Context_Object_" + extId, 
						authCode.getContextObjects().get(extId)
				);
				extIds.add(extId);
			}
			authDoc.replaceItemValue("Context_ExtIds", extIds);
			DominoUtils.computeAndSave(authDoc);
		} finally {
			DominoUtils.recycleQuietly(authDoc);
		}
	}
	
	/**
	 * Loads an authorization code
	 * @param code the code
	 * @return the authorization code (or null if it does not exists)
	 */
	public AuthorizationCode findAuthorizationCode(String code) throws InvalidGrantException, NotesException {
		Document authDoc = null;
		View v = null;
		try {
			v = this.getOauth2DatabaseAsUser().getView(VIEW_AUTHCODES);
			authDoc = v.getDocumentByKey(code, true);
			if( authDoc == null )
				throw new InvalidGrantException();
			AuthorizationCode authCode = DominoUtils.fillObject(new AuthorizationCode(), authDoc);		// Maps are not persisted
			
			Vector<String> extIds = authDoc.getItemValue("Context_ExtIds");
			authCode.setContextClasses(new HashMap<String, String>());
			authCode.setContextObjects(new HashMap<String, String>());
			for( String extId : extIds ) {
				String className = authDoc.getItemValueString("Context_Class_" + extId);
				if( className.length() == 0 )
					continue;
				authCode.getContextClasses().put(extId, className);
				
				String json = authDoc.getItemValueString("Context_Object_" + extId);
				authCode.getContextObjects().put(extId, json);
			}
			
			return authCode;
		} finally {
			DominoUtils.recycleQuietly(authDoc);
			DominoUtils.recycleQuietly(v);
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
}
