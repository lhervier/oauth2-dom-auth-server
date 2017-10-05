package com.github.lhervier.domino.oauth.server.services;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
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
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.utils.Base64Utils;
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
	 * Le nom de la vue de laquelle récupérer les utilisateurs
	 */
	private static final String VIEW_USERS = "($VIMPeople)";
	
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
	 * Notre générateur de nombres aléatoires
	 */
	private static final SecureRandom RANDOM = new SecureRandom();
	
	/**
	 * The server context
	 */
	@Autowired
	private NabService nabBean;
	
	/**
	 * The application root
	 */
	@Value("${oauth2.server.applicationRoot}")
	private String applicationRoot;
	
	/**
	 * Retourne le document Person associé à une app
	 * @param appName le nom de l'app
	 * @return le document
	 * @throws NotesException en cas de pb
	 */
	private Document getPersonDoc(String appName) throws NotesException {
		Name appNotesName = null;
		View v = null;
		try {
			appNotesName = this.notesContext.getUserSession().createName(appName + this.applicationRoot);
			v = DominoUtils.getView(this.nabBean.getNab(), VIEW_USERS);
			return v.getDocumentByKey(appNotesName.getAbbreviated());
		} finally {
			DominoUtils.recycleQuietly(v);
			DominoUtils.recycleQuietly(appNotesName);
		}
	}
	
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
	
	/**
	 * Génère un secret
	 * @return un mot de passe aléatoire
	 */
	private String generatePassword() {
		// see https://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
		return new BigInteger(130, RANDOM).toString(32);
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
	 * Return the application that correspond to the currently
	 * logged in user
	 * @return the application
	 */
	public Application getCurrentApplication() throws NotesException {
		Name nn = null;
		try {
			String fullName = this.notesContext.getUserSession().getEffectiveUserName();
			nn = this.notesContext.getUserSession().createName(fullName);
			String appName = nn.getCommon();
			return this.getApplicationFromName(appName);
		} finally {
			DominoUtils.recycleQuietly(nn);
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
		// Vérifie qu'il n'existe pas déjà une app avec ce nom
		Application existing = this.getApplicationFromName(app.getName());
		if( existing != null )
			throw new RuntimeException("Une application avec ce nom existe déjà.");
		
		// Vérifie qu'il n'existe pas déjà une app avec ce client_id
		existing = this.getApplicationFromClientId(app.getClientId());
		if( existing != null )
			throw new RuntimeException("Une application avec ce client_id existe déjà.");
		
		Document person = null;
		Document appDoc = null;
		Name nn = null;
		try {
			// Vérifie qu'elle ne soit pas déjà déclarée dans le carnet d'adresse
			person = this.getPersonDoc(app.getName());
			if( person != null )
				throw new RuntimeException("Une application avec ce nom existe déjà.");
			
			String abbreviated = app.getName() + this.applicationRoot;
			nn = this.notesContext.getUserSession().createName(abbreviated);
			String fullName = nn.toString();
			
			// Créé une nouvelle application dans le NAB (un nouvel utilisateur)
			person = this.nabBean.getNab().createDocument();
			person.replaceItemValue("Form", "Person");
			person.replaceItemValue("Type", "Person");
			person.replaceItemValue("ShortName", app.getName());
			person.replaceItemValue("LastName", app.getName());
			person.replaceItemValue("MailSystem", "100");		// None
			person.replaceItemValue("FullName", fullName);
			String password = this.generatePassword();
			person.replaceItemValue("HTTPPassword", this.notesContext.getUserSession().evaluate("@Password(\"" + password + "\")"));
			person.replaceItemValue("HTTPPasswordChangeDate", this.notesContext.getUserSession().createDateTime(new Date()));
			person.replaceItemValue("$SecurePassword", "1");
			person.replaceItemValue("Owner", this.notesContext.getUserSession().getEffectiveUserName());
			person.replaceItemValue("LocalAdmin", this.notesContext.getUserSession().getEffectiveUserName());
			DominoUtils.computeAndSave(person);
			
			// Créé un nouveau document pour l'application dans la base
			appDoc = this.getOauth2DatabaseAsUser().createDocument();
			appDoc.replaceItemValue("Form", "Application");
			app.setAppReader(fullName);
			DominoUtils.fillDocument(appDoc, app);
			DominoUtils.computeAndSave(appDoc);
			
			// Rafraîchit le NAB pour prise en compte immédiate
			DominoUtils.refreshNab(this.nabBean.getNab());
			
			// Génère le secret
			return Base64Utils.encodeFromUTF8String(abbreviated + ":" + password);
		} finally {
			DominoUtils.recycleQuietly(appDoc);
			DominoUtils.recycleQuietly(nn);
			DominoUtils.recycleQuietly(person);
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
		Document personDoc = null;
		Document appDoc = null;
		try {
			personDoc = this.getPersonDoc(name);
			if( personDoc == null )
				throw new RuntimeException("L'application '" + name + "' n'existe pas dans le carnet d'adresse. Impossible de la supprimer.");
			personDoc.remove(true);
			
			appDoc = this.getAppDocFromName(name);
			if( appDoc == null )
				throw new RuntimeException("L'application '" + name + "' n'existe pas. Impossible de la supprimer.");
			appDoc.remove(true);
			
			DominoUtils.refreshNab(this.nabBean.getNab());
		} finally {
			DominoUtils.recycleQuietly(appDoc);
			DominoUtils.recycleQuietly(personDoc);
		}
	}
}
