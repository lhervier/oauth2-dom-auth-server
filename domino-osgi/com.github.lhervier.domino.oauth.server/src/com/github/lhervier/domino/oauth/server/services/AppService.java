package com.github.lhervier.domino.oauth.server.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import lotus.domino.Document;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.BaseServerComponent;
import com.github.lhervier.domino.oauth.server.entity.PersonEntity;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.repo.PersonRepository;
import com.github.lhervier.domino.oauth.server.utils.DominoUtils;
import com.github.lhervier.domino.oauth.server.utils.ViewIterator;

/**
 * Managed bean pour gérer les applications.
 * ATTENTION: Cette bean utilise les droits de l'utilisateur
 * actuellement connecté.
 * @author Lionel HERVIER
 */
@Service
public class AppService extends BaseServerComponent {
	
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
	 * The Person repository
	 */
	@Autowired
	private PersonRepository personRepo;
	
	/**
	 * The application root
	 */
	@Value("${oauth2.server.applicationRoot}")
	private String applicationRoot;
	
	/**
	 * Retourne le document associé à une app
	 * @param appName le nom de l'app
	 * @return le document
	 * @throws NotesException en cas de pb
	 */
	private Document getAppDocFromName(String appName) throws NotesException {
		View v = null;
		try {
			v = DominoUtils.getView(this.getOauth2DatabaseAsUser(), VIEW_APPLICATIONS);
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
	private Document getAppDocFromClientId(String clientId) throws NotesException {
		View v = null;
		try {
			v = DominoUtils.getView(this.getOauth2DatabaseAsUser(), VIEW_APPLICATIONS);
			v.resortView(COLUMN_CLIENTID);
			return v.getDocumentByKey(clientId, true);
		} finally {
			DominoUtils.recycleQuietly(v);
		}
	}
	
	// ====================================================================================================
	
	/**
	 * Retourne les noms des applications
	 * @return les noms des applications
	 * @throws NotesException en cas de pb
	 */
	public List<String> getApplicationsNames() throws NotesException {
		List<String> ret = new ArrayList<String>();
		ViewIterator it = null;
		try {
			it = ViewIterator.create().onDatabase(this.getOauth2DatabaseAsUser()).onView(VIEW_APPLICATIONS).sortOnColumn(COLUMN_NAME);
			for( ViewEntry entry : it )
				ret.add((String) entry.getColumnValues().get(0));
			return ret;
		} finally {
			DominoUtils.recycleQuietly(it);
		}
	}
	
	/**
	 * Return an application from its Notes Full Name
	 * @param appFullName the app full name
	 * @return the application or null if it does not exists
	 * @throws NotesException 
	 */
	public Application getApplicationFromFullName(String appFullName) throws NotesException {
		Name n = null;
		try {
			n = this.notesContext.getServerSession().createName(appFullName);
			if( !n.toString().endsWith(this.applicationRoot) )
				return null;
			return this.getApplicationFromName(n.getCommon());
		} finally {
			DominoUtils.recycleQuietly(n);
		}
	}
	
	/**
	 * Retourne une application depuis son nom
	 * @param appName le nom de l'application
	 * @return l'application
	 * @throws NotesException en cas de pb
	 */
	public Application getApplicationFromName(String appName) throws NotesException {
		Document doc = null;
		try {
			doc = this.getAppDocFromName(appName);
			if( doc == null )
				return null;
			return DominoUtils.fillObject(new Application(), doc);
		} finally {
			DominoUtils.recycleQuietly(doc);
		}
	}
	
	/**
	 * Retourne une application depuis son client_id
	 * @param clientId l'id du client
	 * @return l'application
	 * @throws NotesException en cas de pb
	 */
	public Application getApplicationFromClientId(String clientId) throws NotesException {
		Document doc = null;
		try {
			doc = this.getAppDocFromClientId(clientId);
			if( doc == null )
				return null;
			return DominoUtils.fillObject(new Application(), doc);
		} finally {
			DominoUtils.recycleQuietly(doc);
		}
	}
	
	/**
	 * Prépare une future nouvelle app
	 * @return une nouvelle app (avec un client_id seulement)
	 */
	public Application prepareApplication() {
		Application ret = new Application();
		ret.setName("");
		ret.setClientId(UUID.randomUUID().toString());
		ret.setRedirectUri("");
		ret.setRedirectUris(new ArrayList<String>());
		ret.setReaders("*");
		return ret;
	}
	
	/**
	 * Ajoute une application
	 * @param app l'application à ajouter
	 * @return le secret
	 * @throws NotesException en cas de pb
	 */
	public String addApplication(Application app) throws NotesException {
		// Compute full name
		app.setFullName("CN=" + app.getName() + this.applicationRoot);
		
		// Vérifie qu'il n'existe pas déjà une app avec ce nom
		Application existing = this.getApplicationFromName(app.getName());
		if( existing != null )
			throw new RuntimeException("Une application avec ce nom existe déjà.");
		
		// Vérifie qu'il n'existe pas déjà une app avec ce client_id
		existing = this.getApplicationFromClientId(app.getClientId());
		if( existing != null )
			throw new RuntimeException("Une application avec ce client_id existe déjà.");
		
		Document appDoc = null;
		Name nn = null;
		try {
			// Vérifie qu'elle ne soit pas déjà déclarée dans le carnet d'adresse
			PersonEntity person = this.personRepo.findOne(app.getFullName());
			if( person != null )
				throw new RuntimeException("Une application avec ce nom existe déjà.");
			
			// Créé une nouvelle application dans le NAB (un nouvel utilisateur)
			person = new PersonEntity();
			person.setFullNames(Arrays.asList(app.getFullName(), app.getClientId()));
			person.setLastName(app.getName());
			person.setShortName(app.getName());
			person = this.personRepo.save(person);
			String pwd = person.getHttpPassword();
			
			// Créé un nouveau document pour l'application dans la base
			appDoc = this.getOauth2DatabaseAsUser().createDocument();
			appDoc.replaceItemValue("Form", "Application");
			app.setAppReader(app.getFullName());
			DominoUtils.fillDocument(appDoc, app);
			DominoUtils.computeAndSave(appDoc);
			
			return pwd;
		} finally {
			DominoUtils.recycleQuietly(appDoc);
			DominoUtils.recycleQuietly(nn);
		}
	}
	
	/**
	 * Met à jour une application
	 * @param app l'application à mettre à jour
	 * @throws NotesException en cas de pb
	 */
	public void updateApplication(Application app) throws NotesException {
		// Sanity check: Vérifie qu'on n'essaie pas de changer son nom ou son clientId
		Application existing = this.getApplicationFromName(app.getName());
		if( existing == null )
			throw new RuntimeException("Je ne trouve pas l'application '" + app.getName() + "'");
		if( !existing.getClientId().equals(app.getClientId()) )
			throw new RuntimeException("Impossible de changer le clientId");
		existing = this.getApplicationFromClientId(app.getClientId());
		if( existing == null )
			throw new RuntimeException("Je ne toruve pas l'application avec le client_id '" + app.getClientId() + "'");
		if( !existing.getName().equals(app.getName()) )
			throw new RuntimeException("Impossible de changer le nom");
		
		// Vérifie que les URIs sont bien des URIs absolues
		// Et qu'elles ne contiennent pas de fragments
		List<URI> uris = new ArrayList<URI>();
		try {
			for( String uri : app.getRedirectUris() )
				uris.add(new URI(uri));
			uris.add(new URI(app.getRedirectUri()));
		} catch (URISyntaxException e) {
			// May not happend...
			throw new RuntimeException(e);
		}
		for( URI uri : uris ) {
			if( !uri.isAbsolute() )
				throw new RuntimeException("L'uri '" + uri.toString() + "' n'est pas absolue");
			if( uri.toString().indexOf('#') != -1 )
				throw new RuntimeException("L'uri '" + uri.toString() + "' ne doit pas contenir de fragment (#)");
		}
		
		// Extract full name from existing
		app.setFullName(existing.getFullName());
		
		// Update document
		Document appDoc = null;
		try {
			appDoc = this.getAppDocFromName(app.getName());
			if( appDoc == null )
				throw new RuntimeException("Ne devrait pas se produire...");		// On a vérifié qu'il existe juste avant...
			
			DominoUtils.fillDocument(appDoc, app);
			DominoUtils.computeAndSave(appDoc);
		} finally {
			DominoUtils.recycleQuietly(appDoc);
		}
	}
	
	/**
	 * Supprime une application
	 * @param name le nom de l'application
	 * @throws NotesException en cas de pb
	 */
	public void removeApplication(String name) throws NotesException {
		Application app = this.getApplicationFromName(name);
		if( app == null )
			return;
		
		Document appDoc = null;
		try {
			// Remove person in nab
			this.personRepo.delete(app.getFullName());
			
			// Remove document in oauth2 database
			appDoc = this.getAppDocFromName(name);
			if( appDoc != null )
				appDoc.remove(true);
		} finally {
			DominoUtils.recycleQuietly(appDoc);
		}
	}
}
