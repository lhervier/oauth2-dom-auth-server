package fr.asso.afer.oauth2;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import lotus.domino.Base;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;

/**
 * Méthodes pratiques pour Domino
 * @author Lionel HERVIER
 */
public class DominoUtils {

	/**
	 * Pour recycler un object Domino
	 * @param o l'objet à recycler
	 */
	public static final void recycleQuietly(Base o) {
		if( o == null )
			return;
		try {
			o.recycle();
		} catch(NotesException e) {
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * Ouvre une database
	 * @param session la session pour ouvrir la base
	 * @param filePath le chemin vers la base
	 * @return la database ou null si elle n'existe pas
	 * @throws NotesException si on n'a pas les droits
	 */
	public static final Database openDatabase(Session session, String name) throws NotesException {
		Database db = session.getDatabase(null, name, false);		// createOnFail = false
		if( db == null )
			return null;
		if( !db.isOpen() )
			if( !db.open() ) 
				return null;
		return db;
	}
	
	/**
	 * Retourne la liste des valeurs d'un champ
	 * @param doc le document
	 * @param field le nom du champ
	 * @param cl le type d'objet que l'on attend
	 * @return la liste des valeurs
	 * @throws NotesException en cas de pb
	 */
	@SuppressWarnings("unchecked")
	public static final <T> List<T> getItemValue(Document doc, String field, Class<T> cl) throws NotesException {
		List<T> ret = new ArrayList<T>();
		Vector<T> values = doc.getItemValue(field);
		if( values == null )
			return ret;
		ret.addAll(values);
		return ret;
	}
	
	/**
	 * Remplace la valeur d'un champ multiple
	 * @param doc le document
	 * @param field le nom du champ
	 * @param values les valeurs sous forme d'une liste
	 * @throws NotesException en cas de pb
	 */
	public static final <T> void replaceItemValue(Document doc, String field, List<T> values) throws NotesException {
		Vector<T> v = new Vector<T>();
		if( values != null )
			v.addAll(values);
		doc.replaceItemValue(field, v);
	}
}
