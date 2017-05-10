package fr.asso.afer.oauth2;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;
import sun.misc.BASE64Encoder;
import fr.asso.afer.oauth2.model.Application;
import fr.asso.afer.oauth2.utils.DominoUtils;
import fr.asso.afer.oauth2.utils.JSFUtils;

/**
 * Bean pour gérer la liste des applications déclarées
 * @author Lionel HERVIER
 */
public class ApplicationsRegistry {
	
	/**
	 * Le nom de la vue ($Groups)
	 */
	public static final String VIEW_USERS_GROUPS = "($VIMPeopleAndGroups)";
	
	/**
	 * Le nom du champ qui contient la liste des membres
	 */
	public static final String FIELD_MEMBERS = "Members";
	
	/**
	 * Notre générateur de nombres aléatoires
	 */
	private static final SecureRandom RANDOM = new SecureRandom();
	
	/**
	 * La session (injectée par JSF)
	 */
	private Session session;
	
	/**
	 * Retourne la base carnet d'adresse.
	 * @return le NAB
	 * @throws NotesException en cas de pb
	 */
	private Database getNab() throws NotesException {
		Database names = DominoUtils.openDatabase(
				this.session, 
				JSFUtils.getParamsBean().getNab()
		);
		if( names == null )
			throw new RuntimeException("Je n'arrive pas à ouvrir la base '" + JSFUtils.getParamsBean().getNab() + "'");
		return names;
	}
	
	/**
	 * Retourne la vue dans laquelle on peut rechercher des utilisateurs/groupes
	 * @return la vue
	 * @throws NotesException en cas de pb
	 */
	private View getUsersGroupsView() throws NotesException {
		Database names = this.getNab();
		View v = names.getView(VIEW_USERS_GROUPS);
		if( v == null )
			throw new RuntimeException("Je ne trouve pas la vue '" + VIEW_USERS_GROUPS + "' dans le carnet d'adresse.");
		v.setAutoUpdate(false);
		return v;
	}
	
	/**
	 * Pour rafraîchir la vue des applications
	 * @throws NotesException en cas de pb
	 */
	private void refreshUsersGroupsView() throws NotesException {
		View v = null;
		try {
			v = this.getUsersGroupsView();
			v.refresh();
		} finally {
			DominoUtils.recycleQuietly(v);
		}
	}
	
	/**
	 * Retourne le document Notes qui supporte le groupe des applications
	 * @throws NotesException en cas de pb
	 */
	private Document getGroupDoc() throws NotesException {
		View v = null;
		try {
			v = this.getUsersGroupsView();
			Document groupDoc = v.getDocumentByKey(Constants.GROUP_APPLICATIONS);
			if( groupDoc == null )
				throw new RuntimeException("Je ne trouve pas le groupe '" + Constants.GROUP_APPLICATIONS + "' dans le carnet d'adresse");
			return groupDoc;
		} finally {
			DominoUtils.recycleQuietly(v);
		}
	}
	
	/**
	 * Retourne la liste des noms (Notes) applications
	 * @return les noms des applications
	 * @throws NotesException en cas de pb
	 */
	private List<String> getApplicationNames() throws NotesException {
		Document groupDoc = null;
		try {
			groupDoc = this.getGroupDoc();
			return DominoUtils.getItemValue(groupDoc, FIELD_MEMBERS, String.class);
		} finally {
			DominoUtils.recycleQuietly(groupDoc);
		}
	}
	
	/**
	 * Retourne le document correspondant à une application
	 * @param clientId l'id client de l'application
	 * @return le document Person associé ou null s'il n'existe pas
	 * @throws NotesException en cas de pb
	 */
	private Document getAppDocFromClientId(String clientId) throws NotesException {
		List<String> applications = this.getApplicationNames();
		
		View usersGroupsView = null;
		try {
			usersGroupsView = this.getUsersGroupsView();
			
			for( String appName : applications ) {
				Name appNotesName = null;
				try {
					appNotesName = this.session.createName(appName);
					Document appDoc = usersGroupsView.getDocumentByKey(appNotesName.getAbbreviated());
					if( appDoc == null )
						return null;
					String currClientId = appDoc.getItemValueString(Constants.FIELD_CLIENT_ID);
					if( clientId.equals(currClientId) )
						return appDoc;
					appDoc.recycle();
				} finally {
					DominoUtils.recycleQuietly(appNotesName);
				}
			}
			return null;
		} finally {
			DominoUtils.recycleQuietly(usersGroupsView);
		}
	}
	
	/**
	 * Retourne le document correspondant à une application
	 * @param name le nom (long) de l'application
	 * @return le document ou null s'il n'existe pas
	 * @throws NotesException en cas de pb
	 */
	private Document getAppDocFromName(String appName) throws NotesException {
		View usersGroupsView = null;
		Name appNotesName = null;
		try {
			usersGroupsView = this.getUsersGroupsView();
			appNotesName = this.session.createName(appName);
			return usersGroupsView.getDocumentByKey(appNotesName.getAbbreviated());
		} finally {
			DominoUtils.recycleQuietly(appNotesName);
			DominoUtils.recycleQuietly(usersGroupsView);
		}
	}
	
	/**
	 * Génère une application depuis un document
	 * @param doc le document
	 * @return l'application
	 * @throws NotesException en cas de pb
	 */
	private Application appFromDoc(Document doc) throws NotesException {
		Application app = new Application();
		app.setName(doc.getItemValueString("LastName"));
		app.setClientId(doc.getItemValueString(Constants.FIELD_CLIENT_ID));
		app.setRedirectUri(doc.getItemValueString(Constants.FIELF_REDIRECT_URI));
		app.setRedirectUris(DominoUtils.getItemValue(doc, Constants.FIELD_REDIRECT_URIS, String.class));
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
	
	/**
	 * Met à jour un document person avec les infos d'une application
	 * @param doc le document
	 * @param app l'application
	 * @throws NotesException en cas de pb
	 */
	private void updateDoc(Document doc, Application app) throws NotesException {
		doc.replaceItemValue(Constants.FIELF_REDIRECT_URI, app.getRedirectUri());
		Vector<String> values = new Vector<String>();
		values.addAll(app.getRedirectUris());
		doc.replaceItemValue(Constants.FIELD_REDIRECT_URIS, values);
		
		if( !doc.computeWithForm(true, true) )
			throw new RuntimeException("Erreur à la création du document Person");
		doc.save();
	}
	
	/**
	 * Met à jour le NAB
	 * @throws NotesException en cas de pb
	 */
	private void refreshNab() throws NotesException {
		// FIXME: Attendre que les updall se terminent !!
		Session session = this.session;
		session.sendConsoleCommand(session.getServerName(), "load updall -R names.nsf");
//		session.sendConsoleCommand(session.getServerName(), "load updall names.nsf -t \"($Users)\" -r");
//		session.sendConsoleCommand(session.getServerName(), "dbcache flush");
//		session.sendConsoleCommand(session.getServerName(), "tell adminp process people");
		session.sendConsoleCommand(session.getServerName(), "show nlcache reset");
	}
	
	// =============================================================================================================
	
	/**
	 * Retourne la liste des applications
	 * @throws NotesException en cas de pb
	 */
	public List<Application> getApplications() throws NotesException {
		List<String> applications = this.getApplicationNames();
		List<Application> ret = new ArrayList<Application>();
		for( String appName : applications ) {
			Document appDoc = null;
			try {
				appDoc = this.getAppDocFromName(appName);
				if( appDoc == null )
					continue;		// L'application est dans le groupe, mais elle n'existe plus...
				Application app = this.appFromDoc(appDoc);
				ret.add(app);
			} finally {
				DominoUtils.recycleQuietly(appDoc);
			}
		}
		return ret;
	}
	
	/**
	 * Retourne une application à partir de son clientId
	 * @param clientId l'id client
	 * @return l'application
	 * @throws NotesException en cas de pb
	 */
	public Application getApplication(String clientId) throws NotesException {
		Document appDoc = null;
		try {
			appDoc = this.getAppDocFromClientId(clientId);
			return appFromDoc(appDoc);
		} finally {
			DominoUtils.recycleQuietly(appDoc);
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
		return ret;
	}
	
	/**
	 * Supprime une application
	 * @param clientId l'id client de l'application
	 * @throws NotesException en cas de pb
	 */
	public void removeApplication(String clientId) throws NotesException {
		Document appDoc = null;
		Document groupDoc = null;
		try {
			// Récupère l'application
			Application app = this.getApplication(clientId);
			
			// Détruit le document
			appDoc = this.getAppDocFromClientId(clientId);
			if( appDoc != null )
				appDoc.remove(true);
			
			// Met à jour le groupe
			groupDoc = this.getGroupDoc();
			List<String> members = DominoUtils.getItemValue(groupDoc, FIELD_MEMBERS, String.class);
			members.remove(app.getName() + Constants.SUFFIX_APP);
			DominoUtils.replaceItemValue(groupDoc, FIELD_MEMBERS, members);
			if( !groupDoc.computeWithForm(true, true) )
				throw new RuntimeException("Erreur pendant la mise à jour du groupe");
			groupDoc.save();
		} finally {
			DominoUtils.recycleQuietly(groupDoc);
			DominoUtils.recycleQuietly(appDoc);
		}
		this.refreshUsersGroupsView();
		this.refreshNab();
	}
	
	/**
	 * Ajoute une application
	 * @param app l'application à ajouter
	 * @return le secret
	 * @throws NotesException en cas de pb
	 */
	public String addApplication(Application app) throws NotesException {
		Database nab = this.getNab();
		Document person = null;
		Document group = null;
		try {
			// Vérifie qu'il n'existe pas déjà une app avec ce client_id
			person = this.getAppDocFromClientId(app.getClientId());
			if( person != null )
				throw new RuntimeException("Une application avec ce client_id existe déjà.");
			
			// Vérifie qu'il n'existe pas déjà une app avec ce nom
			person = this.getAppDocFromName(app.getName());
			if( person != null )
				throw new RuntimeException("Une application avec ce nom existe déjà.");
			
			// Créé une nouvelle application (un nouvel utilisateur)
			person = nab.createDocument();
			String abbreviated = app.getName() + Constants.SUFFIX_APP;
			String fullName = this.session.createName(abbreviated).toString();
			
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
			
			this.updateDoc(person, app);
			
			// Ajoute le nom de cet utilisateur dans le groupe
			group = this.getGroupDoc();
			List<String> apps = DominoUtils.getItemValue(group, FIELD_MEMBERS, String.class);
			apps.add(fullName);
			DominoUtils.replaceItemValue(group, FIELD_MEMBERS, apps);
			if( !group.computeWithForm(true, true) )
				throw new RuntimeException("Erreur pendant l'ajout de l'application '" + app.getName() + "' au groupe des applications");
			group.save();
			
			// Génère le secret
			BASE64Encoder encoder = new BASE64Encoder();
			String secret;
			try {
				secret = encoder.encode((abbreviated + ":" + password).getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("UTF8 non supporté ???");
			}
			
			// Rafraîchit la vue
			this.refreshUsersGroupsView();
			this.refreshNab();
			
			return secret;
		} finally {
			DominoUtils.recycleQuietly(group);
			DominoUtils.recycleQuietly(person);
		}
	}
	
	/**
	 * Sauvegarde une application existante. Cette action ne permet pas de changer
	 * le nom, le client_id ou le mot de passe.
	 * @param app l'application à mettre à jour.
	 * @throws NotesException en cas de pb
	 */
	public void saveApplication(Application app) throws NotesException {
		Document person = null;
		try {
			// Trouve le doc à partir du nom
			person = this.getAppDocFromName(app.getName());
			if( person == null )
				throw new RuntimeException("L'application '" + app.getName() + "' n'existe pas...");			
			
			// Vérifie que le client Id est OK
			String clientId = person.getItemValueString(Constants.FIELD_CLIENT_ID);
			if( !clientId.equals(app.getClientId()) )
				throw new RuntimeException("Le client_id ne peut pas être changé !");
			
			// Met à jour les infos
			this.updateDoc(person, app);
			
			// Rafraîchit la vue. Pas forcement nécessaire ici...
			this.refreshUsersGroupsView();
			this.refreshNab();
		} finally {
			DominoUtils.recycleQuietly(person);
		}
	}

	// =========================================================================================
	
	/**
	 * @return the session
	 */
	public Session getSession() {
		return session;
	}

	/**
	 * @param session the session to set
	 */
	public void setSession(Session session) {
		this.session = session;
	}
}
