package fr.asso.afer.oauth2;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

/**
 * Méthodes pratiques pour Domino
 * @author Lionel HERVIER
 */
public class DominoUtils {

	/**
	 * Ouvre une database
	 * @param filePath le chemin vers la base
	 * @return la database ou null si elle n'existe pas
	 * @throws NotesException si on n'a pas les droits
	 */
	public static final Database openDatabase(String name) throws NotesException {
		Session session = JSFUtils.getSession();
		Database db = session.getDatabase(null, name, false);		// createOnFail = false
		if( db == null )
			return null;
		if( !db.isOpen() )
			if( !db.open() ) 
				return null;
		return db;
	}
}
