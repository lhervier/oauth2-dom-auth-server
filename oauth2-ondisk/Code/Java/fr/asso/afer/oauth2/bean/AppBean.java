package fr.asso.afer.oauth2.bean;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import sun.misc.BASE64Encoder;
import fr.asso.afer.oauth2.Constants;
import fr.asso.afer.oauth2.model.Application;
import fr.asso.afer.oauth2.utils.DominoUtils;
import fr.asso.afer.oauth2.utils.JSFUtils;
import fr.asso.afer.oauth2.utils.Utils;
import fr.asso.afer.oauth2.utils.ViewIterator;

/**
 * Managed bean pour gérer les applications
 * @author Lionel HERVIER
 */
public class AppBean {
	
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
	 * Le nom du champ qui contient le nom des applications
	 */
	private static final String FIELD_NAME = "Name";
	
	/**
	 * Le nom du champ qui contient le client_id des applications (dans un document Person)
	 */
	private static final String FIELD_CLIENT_ID = "ClientId";
	
	/**
	 * Le nom du champ qui contient l'URL de redirection par défaut d'une application (dans un document Person)
	 */
	public static final String FIELD_REDIRECT_URI = "RedirectUri";
	
	/**
	 * Le nom du champ qui contient les autres URL de redirection d'une application (dans un document Person)
	 */
	public static final String FIELD_REDIRECT_URIS = "RedirectUris";
	
	/**
	 * Le nom du champ qui contient les lecteurs d'une application
	 */
	public static final String FIELD_READERS = "Readers";
	
	/**
	 * Notre générateur de nombres aléatoires
	 */
	private static final SecureRandom RANDOM = new SecureRandom();
	
	/**
	 * La database courante
	 */
	private Database database;
	
	/**
	 * La session
	 */
	private Session sessionAsSigner;
	
	/**
	 * Le nab
	 */
	private Database nab;
	
	/**
	 * Constructeur
	 * @throws NotesException en cas de pb
	 */
	public AppBean() throws NotesException {
		this.sessionAsSigner = JSFUtils.getSessionAsSigner();
		this.nab = Utils.getNab(this.sessionAsSigner);
		
		this.database = JSFUtils.getDatabase();
	}
	
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
			appNotesName = this.sessionAsSigner.createName(appName + Constants.SUFFIX_APP);
			v = DominoUtils.getView(this.nab, VIEW_USERS);
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
			v = DominoUtils.getView(this.database, VIEW_APPLICATIONS);
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
			v = DominoUtils.getView(this.database, VIEW_APPLICATIONS);
			v.resortView(COLUMN_CLIENTID);
			return v.getDocumentByKey(clientId, true);
		} finally {
			DominoUtils.recycleQuietly(v);
		}
	}
	
	/**
	 * Transforme un document en une application
	 * @param doc le document
	 * @return l'application
	 * @throws NotesException en cas de pb
	 */
	private Application appFromDoc(Document doc) throws NotesException {
		Application app = new Application();
		app.setName(doc.getItemValueString(FIELD_NAME));
		app.setClientId(doc.getItemValueString(FIELD_CLIENT_ID));
		app.setRedirectUri(doc.getItemValueString(FIELD_REDIRECT_URI));
		app.setRedirectUris(DominoUtils.getItemValue(doc, FIELD_REDIRECT_URIS, String.class));
		app.setReaders(DominoUtils.getItemValue(doc, FIELD_READERS, String.class));
		return app;
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
			it = ViewIterator.create().onDatabase(this.database).onView(VIEW_APPLICATIONS).sortOnColumn(COLUMN_NAME);
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
			return this.appFromDoc(doc);
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
			return this.appFromDoc(doc);
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
		ret.setClientId(UUID.randomUUID().toString());
		ret.setRedirectUris(new ArrayList<String>());
		ret.setReaders(new ArrayList<String>());
		ret.getReaders().add("*");
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
			
			String abbreviated = app.getName() + Constants.SUFFIX_APP;
			nn = this.sessionAsSigner.createName(abbreviated);
			String fullName = nn.toString();
			
			// Créé une nouvelle application (un nouvel utilisateur)
			person = this.nab.createDocument();
			person.replaceItemValue("Form", "Person");
			person.replaceItemValue("Type", "Person");
			person.replaceItemValue("ShortName", app.getName());
			person.replaceItemValue("LastName", app.getName());
			person.replaceItemValue("MailSystem", "100");		// None
			person.replaceItemValue("FullName", fullName);
			String password = this.generatePassword();
			person.replaceItemValue("HTTPPassword", this.sessionAsSigner.evaluate("@Password(\"" + password + "\")"));
			person.replaceItemValue("HTTPPasswordChangeDate", this.sessionAsSigner.createDateTime(new Date()));
			person.replaceItemValue("$SecurePassword", "1");
			person.replaceItemValue("Owner", this.sessionAsSigner.getEffectiveUserName());
			person.replaceItemValue("LocalAdmin", this.sessionAsSigner.getEffectiveUserName());
			DominoUtils.computeAndSave(person);
			
			// Créé un nouveau document pour l'application dans la base
			appDoc = this.database.createDocument();
			appDoc.replaceItemValue("Form", "Application");
			appDoc.replaceItemValue(FIELD_NAME, app.getName());
			appDoc.replaceItemValue(FIELD_CLIENT_ID, app.getClientId());
			appDoc.replaceItemValue(FIELD_REDIRECT_URI, app.getRedirectUri());
			DominoUtils.replaceItemValue(appDoc, FIELD_REDIRECT_URIS, app.getRedirectUris());
			DominoUtils.replaceItemValue(appDoc, FIELD_READERS, app.getReaders());
			DominoUtils.computeAndSave(appDoc);
			
			// Rafraîchit le NAB pour prise en compte immédiate
			DominoUtils.refreshNab(this.nab);
			
			// Génère le secret
			try {
				return new BASE64Encoder().encode((abbreviated + ":" + password).getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("UTF8 non supporté ???");
			}
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
		
		Document appDoc = null;
		try {
			appDoc = this.getAppDocFromName(app.getName());
			if( appDoc == null )
				throw new RuntimeException("Ne devrait pas se produire...");		// On a vérifié qu'il existe juste avant...
			
			appDoc.replaceItemValue(FIELD_REDIRECT_URIS, app.getRedirectUri());
			DominoUtils.replaceItemValue(appDoc, FIELD_REDIRECT_URI, app.getRedirectUris());
			DominoUtils.replaceItemValue(appDoc, "Readers", app.getReaders());
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
			
			DominoUtils.refreshNab(this.nab);
		} finally {
			DominoUtils.recycleQuietly(appDoc);
			DominoUtils.recycleQuietly(personDoc);
		}
	}
}
