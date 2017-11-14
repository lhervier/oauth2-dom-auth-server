package com.github.lhervier.domino.oauth.server.repo.impl.notes;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewEntry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Repository;

import com.github.lhervier.domino.oauth.server.AuthContext;
import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.repo.ApplicationRepository;
import com.github.lhervier.domino.oauth.server.utils.DominoUtils;
import com.github.lhervier.domino.oauth.server.utils.ViewIterator;

/**
 * Repository to access applications
 * @author Lionel HERVIER
 */
@Repository
public class NotesApplicationRepository implements ApplicationRepository {
	
	/**
	 * Le nom de la vue qui contient toutes les applications
	 */
	private static final String VIEW_APPLICATIONS = "Applications";
	
	/**
	 * Le nom de la colonne pour trier les apps par nom
	 */
	private static final String COLUMN_NAME = "Name";
	
	/**
	 * Le nom de la colonne pour trier par client id
	 */
	private static final String COLUMN_CLIENTID = "ClientId";
	
	/**
	 * The database where to store application information
	 */
	@Value("${oauth2.server.db}")
	private String oauth2db;
	
	/**
	 * The authentication context
	 */
	@Autowired
	protected AuthContext authCtx;
	
	/**
	 * Return the oauth2 database as the user (the application)
	 * @throws NotesException 
	 */
	private Database getOauth2Database(Session session) throws NotesException {
		return DominoUtils.openDatabase(session, this.oauth2db);
	}
	
	/**
	 * Retourne le document associé à une app
	 * @param appName le nom de l'app
	 * @return le document
	 * @throws NotesException en cas de pb
	 */
	private Document findOneDocByName(Session session, String appName) throws NotesException {
		View v = null;
		try {
			v = DominoUtils.getView(this.getOauth2Database(session), VIEW_APPLICATIONS);
			v.resortView(COLUMN_NAME);
			return v.getDocumentByKey(appName, true);
		} finally {
			DominoUtils.recycleQuietly(v);
		}
	}
	
	/**
	 * Retourne le document associé à une app
	 * @param clientId l'id client de l'app
	 * @return le document
	 * @throws NotesException en cas de pb
	 */
	private Document findOneDoc(Session session, String clientId) throws NotesException {
		View v = null;
		try {
			v = DominoUtils.getView(this.getOauth2Database(session), VIEW_APPLICATIONS);
			v.resortView(COLUMN_CLIENTID);
			return v.getDocumentByKey(clientId, true);
		} finally {
			DominoUtils.recycleQuietly(v);
		}
	}
	
	// ====================================================================================================
	
	/**
	 * Return the applications names.
	 * We are using the user rights to access the oauth2 database.
	 * @return the registered application names
	 */
	public List<String> listNames() {
		Session session = this.authCtx.getUserSession();
		
		List<String> ret = new ArrayList<String>();
		ViewIterator it = null;
		try {
			it = ViewIterator
					.create()
					.onDatabase(this.getOauth2Database(session))
					.onView(VIEW_APPLICATIONS)
					.sortOnColumn(COLUMN_NAME);
			for( ViewEntry entry : it )
				ret.add((String) entry.getColumnValues().get(0));
			return ret;
		} catch(NotesException e) {
			throw new DataRetrievalFailureException("Error getting application names", e);
		} finally {
			DominoUtils.recycleQuietly(it);
		}
	}
	
	/**
	 * Retourne une application depuis son nom
	 * @param appName le nom de l'application
	 * @return l'application
	 */
	public ApplicationEntity findOneByName(String appName) {
		Session session = this.authCtx.getUserSession();
		Document doc = null;
		try {
			doc = this.findOneDocByName(session, appName);
			if( doc == null )
				return null;
			return DominoUtils.fillObject(new ApplicationEntity(), doc);
		} catch(NotesException e) {
			throw new DataRetrievalFailureException("Error getting application detail", e);
		} finally {
			DominoUtils.recycleQuietly(doc);
		}
	}
	
	/**
	 * Retourne une application depuis son client_id
	 * @param clientId l'id du client
	 * @return l'application
	 */
	public ApplicationEntity findOne(String clientId) {
		Session session = this.authCtx.getUserSession();
		
		Document doc = null;
		try {
			doc = this.findOneDoc(session, clientId);
			if( doc == null )
				return null;
			return DominoUtils.fillObject(new ApplicationEntity(), doc);
		} catch(NotesException e) {
			throw new DataRetrievalFailureException("Error getting application detail", e);
		} finally {
			DominoUtils.recycleQuietly(doc);
		}
	}
	
	/**
	 * Add a new application
	 * @param app the application to create
	 */
	public ApplicationEntity save(ApplicationEntity app) {
		Session session = this.authCtx.getUserSession();
		
		// Check that URIs are absolute and does not contain fragments
		// FIXME: URI validations must be made at the service level.
		List<URI> uris = new ArrayList<URI>();
		try {
			if( app.getRedirectUris() != null ) {
				for( String uri : app.getRedirectUris() )
					uris.add(new URI(uri));
			}
			uris.add(new URI(app.getRedirectUri()));
		} catch (URISyntaxException e) {
			throw new DataIntegrityViolationException("Invalid URI", e);
		}
		for( URI uri : uris ) {
			if( !uri.isAbsolute() )
				throw new DataIntegrityViolationException("URI '" + uri.toString() + "' is not absolute");
		}
		
		Document appDoc = null;
		try {
			// New application
			ApplicationEntity existing = this.findOneByName(app.getName());
			if( existing == null ) {
			
				// Check that an application with the same client id does not already exist 
				// FIXME: Nothing to do in the repository
				existing = this.findOne(app.getClientId());
				if( existing != null )
					throw new DataIntegrityViolationException("An application with that client id already exists");
				
				// Create a new document
				appDoc = this.getOauth2Database(session).createDocument();
				appDoc.replaceItemValue("Form", "Application");
				app.setAppReader(app.getFullName());
			
			// Update an existing application
			} else {
				// Check that the user is not trying to change the application client id
				// FIXME: Nothing to do in the repository
				if( !existing.getClientId().equals(app.getClientId()) )
					throw new DataIntegrityViolationException("You cannot change an application client id");
				
				// Get application backend document
				appDoc = this.findOneDocByName(session, app.getName());		// Should not return null here
			}
			
			// Save the document
			DominoUtils.fillDocument(appDoc, app);
			DominoUtils.computeAndSave(appDoc);
		} catch(NotesException e) {
			throw new DataRetrievalFailureException("Error saveing application", e);
		} finally {
			DominoUtils.recycleQuietly(appDoc);
		}
		
		return app;
	}
	
	/**
	 * Supprime une application
	 * @param name le nom de l'application
	 */
	public void deleteByName(String name) {
		Session session = this.authCtx.getUserSession();
		
		Document appDoc = null;
		try {
			appDoc = this.findOneDocByName(session, name);
			if( appDoc != null )
				appDoc.remove(true);
		} catch(NotesException e) {
			throw new DataRetrievalFailureException("Error removing application", e);
		} finally {
			DominoUtils.recycleQuietly(appDoc);
		}
	}
}
