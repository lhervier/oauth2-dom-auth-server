package com.github.lhervier.domino.oauth.common.bean;

import java.io.Serializable;

import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;

import com.github.lhervier.domino.oauth.common.NotesContext;
import com.github.lhervier.domino.oauth.common.utils.DominoUtils;

/**
 * Classe de base pour les bean qui reprennent un document
 * de paramétrage
 * @author Lionel HERVIER
 */
public abstract class BaseParamsBean implements Serializable {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -4578663002805520227L;

	/**
	 * Le nom de la vue à charger
	 */
	private String viewName;
	
	/**
	 * Le préfixe des champs
	 */
	private String prefix;
	
	/**
	 * The notes context
	 */
	private NotesContext notesContext;
	
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
			v = this.notesContext.getServerDatabase().getView(this.viewName);
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
	 */
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	/**
	 * @param prefix the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * @param notesContext the notesContext to set
	 * @throws NotesException 
	 */
	public void setNotesContext(NotesContext notesContext) throws NotesException {
		this.notesContext = notesContext;
		this.reload();
	}
}
