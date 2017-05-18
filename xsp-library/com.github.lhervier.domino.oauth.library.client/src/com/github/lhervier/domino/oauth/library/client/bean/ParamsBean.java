package com.github.lhervier.domino.oauth.library.client.bean;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.github.lhervier.domino.oauth.common.utils.JSFUtils;

public class ParamsBean {

	/**
	 * L'id client de l'appli
	 */
	private String clientId;
	
	/**
	 * Le secret de l'appli
	 */
	private String secret;

	/**
	 * L'URI de base
	 */
	private String baseURI;
	
	/**
	 * End point authorize
	 */
	private String authorizeEndPoint;
	
	/**
	 * end point token
	 */
	private String tokenEndPoint;
	
	/**
	 * Constructeur
	 */
	public ParamsBean() throws NotesException {
		Database db = null;
		View v = null;
		Document doc = null;
		try {
			Session session = JSFUtils.getSessionAsSigner();
			db = DominoUtils.openDatabase(session, JSFUtils.getDatabase().getFilePath());
			v = db.getView("Params");
			if( v.getEntryCount() != 1 )
				throw new RuntimeException("Il doit y avoir un seul document dans la vue Params");
			doc = v.getFirstDocument();
			DominoUtils.fillObject(this, doc);
		} finally {
			DominoUtils.recycleQuietly(doc);
			DominoUtils.recycleQuietly(v);
			DominoUtils.recycleQuietly(db);
		}
	}
	
	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the secret
	 */
	public String getSecret() {
		return secret;
	}

	/**
	 * @param secret the secret to set
	 */
	public void setSecret(String secret) {
		this.secret = secret;
	}

	/**
	 * @return the baseURI
	 */
	public String getBaseURI() {
		return baseURI;
	}

	/**
	 * @param baseURI the baseURI to set
	 */
	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	/**
	 * @return the authorizeEndPoint
	 */
	public String getAuthorizeEndPoint() {
		return authorizeEndPoint;
	}

	/**
	 * @param authorizeEndPoint the authorizeEndPoint to set
	 */
	public void setAuthorizeEndPoint(String authorizeEndPoint) {
		this.authorizeEndPoint = authorizeEndPoint;
	}

	/**
	 * @return the tokenEndPoint
	 */
	public String getTokenEndPoint() {
		return tokenEndPoint;
	}

	/**
	 * @param tokenEndPoint the tokenEndPoint to set
	 */
	public void setTokenEndPoint(String tokenEndPoint) {
		this.tokenEndPoint = tokenEndPoint;
	}
}
