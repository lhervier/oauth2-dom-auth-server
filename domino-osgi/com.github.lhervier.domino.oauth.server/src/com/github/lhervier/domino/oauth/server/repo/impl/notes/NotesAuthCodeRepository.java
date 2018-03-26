package com.github.lhervier.domino.oauth.server.repo.impl.notes;

import java.util.HashMap;
import java.util.Vector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.notes.AuthContext;
import com.github.lhervier.domino.oauth.server.notes.DominoUtils;
import com.github.lhervier.domino.oauth.server.notes.NotesRuntimeException;
import com.github.lhervier.domino.oauth.server.repo.AuthCodeRepository;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

/**
 * Service to manage authorization codes
 * @author Lionel HERVIER
 */
@Repository
public class NotesAuthCodeRepository implements AuthCodeRepository {

	/**
	 * Le nom de la vue qui contient les codes autorization
	 */
	private static final String VIEW_AUTHCODES = "AuthorizationCodes";
	
	/**
	 * The auth context
	 */
	@Autowired
	protected AuthContext authContext;
	
	/**
	 * The database where to store application information
	 */
	@Value("${oauth2.server.db}")
	private String oauth2db;
	
	/**
	 * Return the oauth2 database as the server
	 * @param session the session to use to open the database
	 */
	private Database getOauth2Database(Session session) {
		return DominoUtils.openDatabase(session, this.oauth2db);
	}
	
	/**
	 * Save an authorization code
	 * @param authCode the authorization code to save
	 */
	public AuthCodeEntity save(AuthCodeEntity authCode) {
		Session session = this.authContext.getServerSession();
		
		Document authDoc = null;
		try {
			authDoc = this.getOauth2Database(session).createDocument();
			authDoc.replaceItemValue("Form", "AuthorizationCode");
			DominoUtils.fillDocument(authDoc, authCode);		// Will not persist Map properties
			Vector<String> extIds = new Vector<String>();		// NOSONAR
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
		} catch(NotesException e) {
			throw new NotesRuntimeException("Error saving authcode", e);
		} finally {
			DominoUtils.recycleQuietly(authDoc);
		}
		return authCode;
	}
	
	/**
	 * Loads an authorization code
	 * @param code the code
	 * @return the authorization code (or null if it does not exists)
	 */
	@SuppressWarnings("unchecked")
	public AuthCodeEntity findOne(String code) {
		Session session = this.authContext.getUserSession();
		
		Document authDoc = null;
		View v = null;
		try {
			v = this.getOauth2Database(session).getView(VIEW_AUTHCODES);
			authDoc = v.getDocumentByKey(code, true);
			if( authDoc == null )
				return null;
			AuthCodeEntity authCode = DominoUtils.fillObject(new AuthCodeEntity(), authDoc);		// Maps are not persisted
			
			Vector<String> extIds = authDoc.getItemValue("Context_ExtIds");	// NOSONAR
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
		} catch(NotesException e) {
			throw new NotesRuntimeException("Error getting auth code", e);
		} finally {
			DominoUtils.recycleQuietly(authDoc);
			DominoUtils.recycleQuietly(v);
		}
	}
	
	/**
	 * Remove an authorization code
	 * @param code the auth code
	 */
	public boolean delete(String code) {
		Session session = this.authContext.getServerSession();
		
		View v = null;
		Document authDoc = null;
		try {
			// User (application) does not have the rights to remove document from the database
			v = DominoUtils.getView(this.getOauth2Database(session), VIEW_AUTHCODES);
			authDoc = v.getDocumentByKey(code, true);
			if( authDoc == null )
				return false;
			if( !authDoc.remove(true) )
				throw new NotesRuntimeException("Unable to remove auth code document !");
			return true;
		} catch(NotesException e) {
			throw new NotesRuntimeException("Error removing auth code", e);
		} finally {
			DominoUtils.recycleQuietly(authDoc);
			DominoUtils.recycleQuietly(v);
		}
	}
}
