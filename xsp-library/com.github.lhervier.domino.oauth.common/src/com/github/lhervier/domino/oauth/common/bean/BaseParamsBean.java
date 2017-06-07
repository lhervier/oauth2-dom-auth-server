package com.github.lhervier.domino.oauth.common.bean;

import java.io.Serializable;

import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;

import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.github.lhervier.domino.oauth.common.utils.JSFUtils;

/**
 * Classe de base pour les bean qui reprennent un document
 * de param�trage
 * @author Lionel HERVIER
 */
public abstract class BaseParamsBean implements Serializable {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -4578663002805520227L;

	/**
	 * Le nom de la vue � charger
	 */
	private String viewName;
	
	/**
	 * Le pr�fixe des champs
	 */
	private String prefix;
	
	/**
	 * Recharge la configuration
	 * @throws NotesException en cas de pb
	 */
	public void reload() throws NotesException {
		if( this.viewName == null )
			return;
		if( this.prefix == null )
			return;
		
		View v = null;
		Document doc = null;
		try {
			v = JSFUtils.getDatabaseAsSigner().getView(this.viewName);
			if( v.getEntryCount() != 1 )
				throw new RuntimeException("Il doit y avoir un seul document dans la vue '" + this.viewName + "'");
			doc = v.getFirstDocument();
			DominoUtils.fillObject(this, doc, this.prefix);
		} finally {
			DominoUtils.recycleQuietly(doc);
			DominoUtils.recycleQuietly(v);
		}
	}

	/**
	 * @param viewName the viewName to set
	 * @throws NotesException 
	 */
	public void setViewName(String viewName) throws NotesException {
		this.viewName = viewName;
		this.reload();
	}

	/**
	 * @param prefix the prefix to set
	 * @throws NotesException 
	 */
	public void setPrefix(String prefix) throws NotesException {
		this.prefix = prefix;
		this.reload();
	}
}
