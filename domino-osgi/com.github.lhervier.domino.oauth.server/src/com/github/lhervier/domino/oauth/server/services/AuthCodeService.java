package com.github.lhervier.domino.oauth.server.services;

import java.util.HashMap;
import java.util.Vector;

import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;

import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.BaseServerComponent;
import com.github.lhervier.domino.oauth.server.ex.grant.InvalidGrantException;
import com.github.lhervier.domino.oauth.server.model.AuthorizationCode;
import com.github.lhervier.domino.oauth.server.utils.DominoUtils;

/**
 * Service to manage authorization codes
 * @author Lionel HERVIER
 */
@Service
public class AuthCodeService extends BaseServerComponent {

	/**
	 * Le nom de la vue qui contient les codes autorization
	 */
	private static final String VIEW_AUTHCODES = "AuthorizationCodes";
	
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
	@SuppressWarnings("unchecked")
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
