package fr.asso.afer.oauth2.params;

import java.io.Serializable;

import fr.asso.afer.oauth2.utils.DominoUtils;
import fr.asso.afer.oauth2.utils.JSFUtils;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;

/**
 * Bean pour accéder aux paramètres de l'application
 * @author Lionel HERVIER
 */
public class ParamsBean implements Serializable {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 25035453476284192L;

	/**
	 * Le nom de la vue qui contient le doc de param
	 */
	private static final String VIEW_PARAMS = "Params";
	
	/**
	 * Le carnet d'adresse où créer les applications
	 */
	private String nab;
	
	/**
	 * La durée de vie de l'access token
	 */
	private long accessTokenLifetime;
	
	/**
	 * La durée de vie du refresh token
	 */
	private long refreshTokenLifetime;
	
	/**
	 * Constructeur
	 * @throws NotesException en cas de pb
	 */
	public ParamsBean() throws NotesException {
		this.reload();
	}
	
	/**
	 * Pour recharger la configuration
	 * @throws NotesException en cas de problème
	 */
	public void reload() throws NotesException {
		Database database = JSFUtils.getDatabase();
		View v = database.getView(VIEW_PARAMS);
		Document doc = v.getFirstDocument();
		if( doc == null )
			throw new RuntimeException("Le document de paramétrage n'existe pas. Impossible de démarrer l'application.");
		DominoUtils.fillObject(this, doc);
	}
	
	// =============================================================

	/**
	 * @return the nab
	 */
	public String getNab() {
		return nab;
	}

	/**
	 * @param nab the nab to set
	 */
	public void setNab(String nab) {
		this.nab = nab;
	}

	/**
	 * @return the accessTokenLifetime
	 */
	public long getAccessTokenLifetime() {
		return accessTokenLifetime;
	}

	/**
	 * @param accessTokenLifetime the accessTokenLifetime to set
	 */
	public void setAccessTokenLifetime(long accessTokenLifetime) {
		this.accessTokenLifetime = accessTokenLifetime;
	}

	/**
	 * @return the refreshTokenLifetime
	 */
	public long getRefreshTokenLifetime() {
		return refreshTokenLifetime;
	}

	/**
	 * @param refreshTokenLifetime the refreshTokenLifetime to set
	 */
	public void setRefreshTokenLifetime(long refreshTokenLifetime) {
		this.refreshTokenLifetime = refreshTokenLifetime;
	}
}
