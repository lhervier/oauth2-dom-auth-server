package fr.asso.afer.oauth2.app;

import java.util.ArrayList;
import java.util.List;

import lotus.domino.Document;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;
import fr.asso.afer.oauth2.Constants;
import fr.asso.afer.oauth2.utils.DominoUtils;
import fr.asso.afer.oauth2.utils.JSFUtils;
import fr.asso.afer.oauth2.utils.Utils;

/**
 * Managed bean pour gérer le groupe qui mémorise
 * la liste des applications
 * @author Lionel HERVIER
 */
public class AppGroupBean {

	/**
	 * La vue qui contient les groupes
	 */
	private static final String VIEW_GROUPS = "($VIMGroups)";
	
	/**
	 * Le nom du groupe qui contient l'ensemble des applications
	 */
	private static final String GROUP_APPLICATIONS = "OAUTH2Applications";
	
	/**
	 * Le nom du champ qui contient les membres dans un groupes
	 */
	private static final String FIELD_MEMBERS = "Members";
	
	// ====================================================================
	
	/**
	 * La session
	 */
	private Session session;

	/**
	 * La vue pour gérer les groupes
	 */
	private View v;
	
	/**
	 * Le document qui supporte le groupe
	 */
	private Document doc;
	
	/**
	 * Constructeur
	 * @throws NotesException en cas de pb
	 */
	public AppGroupBean() throws NotesException {
		this.session = JSFUtils.getSessionAsSigner();
		this.v = DominoUtils.getView(
				Utils.getNab(this.session), 
				VIEW_GROUPS
		);
		this.doc = this.v.getDocumentByKey(GROUP_APPLICATIONS, true);
		if( this.doc == null )
			throw new RuntimeException("Le groupe '" + GROUP_APPLICATIONS + "' n'existe pas. Vous devez le déclarer.");
	}
	
	/**
	 * Vérifie que l'utilisateur courant peut gérer les applications
	 * @throws RuntimeException si ce n'est pas le cas
	 */
	private void check() {
		if( !JSFUtils.getContext().getUser().getRoles().contains(Constants.ROLE_APPSMANAGER) )
			throw new RuntimeException("Vous n'êtes pas autorisé à gérer les applications sur ce serveur");
	}
	
	// ====================================================================
	
	/**
	 * Retourne la liste des noms des applications déclarées
	 * @return la liste des noms des applications
	 * @throws NotesException en cas de problème
	 */
	public List<String> getApplications() throws NotesException {
		this.check();
		List<String> apps = DominoUtils.getItemValue(this.doc, FIELD_MEMBERS, String.class);
		List<String> ret = new ArrayList<String>();
		for( String fullName : apps ) {
			Name nn = null;
			try {
				nn = this.session.createName(fullName);
				ret.add(nn.getCommon());
			} finally {
				DominoUtils.recycleQuietly(nn);
			}
		}
		return ret;
	}
	
	/**
	 * Ajoute une application dans la liste
	 * @param appName le nom de l'application à ajouter
	 * @throws NotesException en cas de pb
	 */
	public void addApplication(String appName) throws NotesException {
		this.check();
		Name nn = null;
		try {
			nn = this.session.createName(appName + Constants.SUFFIX_APP);
			List<String> apps = DominoUtils.getItemValue(this.doc, FIELD_MEMBERS, String.class);
			apps.add(nn.toString());
			DominoUtils.replaceItemValue(this.doc, FIELD_MEMBERS, apps);
			DominoUtils.computeAndSave(this.doc);
		} finally {
			DominoUtils.recycleQuietly(nn);
		}
	}
	
	/**
	 * Enlève une application de la liste des applications connues
	 * @param appName le nom de l'application
	 * @throws NotesException en cas de problème
	 */
	public void removeApplication(String appName) throws NotesException {
		this.check();
		Name nn = null;
		try {
			nn = this.session.createName(appName + Constants.SUFFIX_APP);
			List<String> apps = DominoUtils.getItemValue(this.doc, FIELD_MEMBERS, String.class);
			apps.remove(nn.toString());
			DominoUtils.replaceItemValue(this.doc, FIELD_MEMBERS, apps);
			DominoUtils.computeAndSave(this.doc);
		} finally {
			DominoUtils.recycleQuietly(nn);
		}
	}
}
