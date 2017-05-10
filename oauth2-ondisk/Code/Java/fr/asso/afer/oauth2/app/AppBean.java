package fr.asso.afer.oauth2.app;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import sun.misc.BASE64Encoder;

import fr.asso.afer.oauth2.Constants;
import fr.asso.afer.oauth2.model.Application;
import fr.asso.afer.oauth2.utils.DominoUtils;
import fr.asso.afer.oauth2.utils.JSFUtils;
import fr.asso.afer.oauth2.utils.Utils;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

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
	 * Le nom du champ qui contient le client_id des applications (dans un document Person)
	 */
	private static final String FIELD_CLIENT_ID = "OAUTH2_client_id";
	
	/**
	 * Le nom du champ qui contient l'URL de redirection par défaut d'une application (dans un document Person)
	 */
	public static final String FIELD_REDIRECT_URI = "OAUTH2_redirect_uri";
	
	/**
	 * Le nom du champ qui contient les autres URL de redirection d'une application (dans un document Person)
	 */
	public static final String FIELD_REDIRECT_URIS = "OAUTH2_redirect_uris";
	
	/**
	 * Notre générateur de nombres aléatoires
	 */
	private static final SecureRandom RANDOM = new SecureRandom();
	
	/**
	 * La session
	 */
	private Session session;
	
	/**
	 * Le nab
	 */
	private Database nab;
	
	/**
	 * La vue pour récupérer les utilisateurs
	 */
	private View v;
	
	/**
	 * La bean pour accéder au groupe des apps
	 */
	private AppGroupBean appGroupBean;
	
	/**
	 * Constructeur
	 * @throws NotesException en cas de pb
	 */
	public AppBean() throws NotesException {
		this.session = JSFUtils.getSessionAsSigner();
		this.nab = Utils.getNab(this.session);
		this.v = DominoUtils.getView(
				this.nab, 
				VIEW_USERS
		);
		this.appGroupBean = JSFUtils.getAppGroupBean();
	}
	
	/**
	 * Retourne le document associé à une app
	 * @param appName le nom de l'app
	 * @return le document
	 * @throws NotesException en cas de pb
	 */
	private Document getDoc(String appName) throws NotesException {
		Name appNotesName = null;
		try {
			appNotesName = this.session.createName(appName + Constants.SUFFIX_APP);
			return this.v.getDocumentByKey(appNotesName.getAbbreviated());
		} finally {
			DominoUtils.recycleQuietly(appNotesName);
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
		app.setName(doc.getItemValueString("LastName"));
		app.setClientId(doc.getItemValueString(FIELD_CLIENT_ID));
		app.setRedirectUri(doc.getItemValueString(FIELD_REDIRECT_URI));
		app.setRedirectUris(DominoUtils.getItemValue(doc, FIELD_REDIRECT_URIS, String.class));
		return app;
	}
	
	/**
	 * Tranfert les infos d'une application dans un doc
	 * @param app l'application
	 * @param doc le document à mettre à jour
	 * @throws NotesException en cas de pb
	 */
	private void appToDoc(Application app, Document doc) throws NotesException {
		doc.replaceItemValue(FIELD_REDIRECT_URI, app.getRedirectUri());
		Vector<String> values = new Vector<String>();
		values.addAll(app.getRedirectUris());
		doc.replaceItemValue(FIELD_REDIRECT_URIS, values);
	}
	
	/**
	 * Génère un secret
	 * @return un mot de passe aléatoire
	 */
	private String generatePassword() {
		// see https://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
		return new BigInteger(130, RANDOM).toString(32);
	}
	
	/**
	 * Vérifie que l'utilisateur courant peut gérer les applications
	 * @throws RuntimeException si ce n'est pas le cas
	 */
	private void check() {
		if( !JSFUtils.getContext().getUser().getRoles().contains(Constants.ROLE_APPSMANAGER) )
			throw new RuntimeException("Vous n'êtes pas autorisé à gérer les applications sur ce serveur");
	}
	
	// ====================================================================================================
	
	/**
	 * Retourne une application depuis son nom
	 * @param appName le nom de l'application
	 * @return l'application
	 * @throws NotesException en cas de pb
	 */
	public Application getApplicationFromName(String appName) throws NotesException {
		this.check();
		Document doc = null;
		try {
			doc = this.getDoc(appName);
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
		this.check();
		List<String> apps = this.appGroupBean.getApplications();
		for( String appName : apps) {
			Application app = this.getApplicationFromName(appName);
			if( app.getClientId().equals(clientId) )
				return app;
		}
		return null;
	}
	
	/**
	 * Prépare une future nouvelle app
	 * @return une nouvelle app (avec un client_id seulement)
	 */
	public Application prepareApplication() {
		this.check();
		Application ret = new Application();
		ret.setClientId(UUID.randomUUID().toString());
		ret.setRedirectUris(new ArrayList<String>());
		return ret;
	}
	
	/**
	 * Ajoute une application
	 * @param app l'application à ajouter
	 * @return le secret
	 * @throws NotesException en cas de pb
	 */
	public String addApplication(Application app) throws NotesException {
		this.check();
		// Vérifie qu'il n'existe pas déjà une app avec ce nom
		Application existing = this.getApplicationFromName(app.getName());
		if( existing != null )
			throw new RuntimeException("Une application avec ce nom existe déjà.");
		
		// Vérifie qu'il n'existe pas déjà une app avec ce client_id
		existing = this.getApplicationFromClientId(app.getClientId());
		if( existing != null )
			throw new RuntimeException("Une application avec ce client_id existe déjà.");
		
		Document person = null;
		Name nn = null;
		try {
			String abbreviated = app.getName() + Constants.SUFFIX_APP;
			nn = this.session.createName(abbreviated);
			String fullName = this.session.createName(abbreviated).toString();
			
			// Créé une nouvelle application (un nouvel utilisateur)
			person = this.nab.createDocument();
			person.replaceItemValue("Form", "Person");
			person.replaceItemValue("Type", "Person");
			person.replaceItemValue("ShortName", app.getName());
			person.replaceItemValue("LastName", app.getName());
			person.replaceItemValue("MailSystem", "100");		// None
			person.replaceItemValue("FullName", fullName);
			String password = this.generatePassword();
			person.replaceItemValue("HTTPPassword", this.session.evaluate("@Password(\"" + password + "\")"));
			person.replaceItemValue("HTTPPasswordChangeDate", this.session.createDateTime(new Date()));
			person.replaceItemValue("$SecurePassword", "1");
			person.replaceItemValue("Owner", this.session.getEffectiveUserName());
			person.replaceItemValue("LocalAdmin", this.session.getEffectiveUserName());
			person.replaceItemValue(Constants.FIELD_CLIENT_ID, app.getClientId());
			
			this.appToDoc(app, person);
			DominoUtils.computeAndSave(person);
			
			// Ajoute le nom de cet utilisateur dans le groupe
			this.appGroupBean.addApplication(app.getName());
			
			// Rafraîchit le NAB pour prise en compte immédiate
			DominoUtils.refreshNab(this.nab);
			
			// Génère le secret
			try {
				return new BASE64Encoder().encode((abbreviated + ":" + password).getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("UTF8 non supporté ???");
			}
		} finally {
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
		this.check();
		// Sanity check
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
		
		Document person = null;
		try {
			person = this.getDoc(app.getName());
			if( person == null )
				throw new RuntimeException("Ne devrait pas se produire...");		// On a vérifié qu'il existe juste avant...
			this.appToDoc(app, person);
		} finally {
			DominoUtils.recycleQuietly(person);
		}
	}
	
	/**
	 * Supprime une application
	 * @param name le nom de l'application
	 * @throws NotesException en cas de pb
	 */
	public void removeApplication(String name) throws NotesException {
		this.check();
		Document appDoc = null;
		try {
			appDoc = this.getDoc(name);
			if( appDoc == null )
				throw new RuntimeException("L'application '" + name + "' n'existe pas. Impossible de la supprimer.");
			appDoc.remove(true);
			this.appGroupBean.removeApplication(name);
			DominoUtils.refreshNab(this.nab);
		} finally {
			DominoUtils.recycleQuietly(appDoc);
		}
	}
}
