package com.github.lhervier.domino.oauth.server.services;

import java.util.Vector;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.BaseServerComponent;
import com.github.lhervier.domino.oauth.server.utils.DominoUtils;

@Service
public class NabService extends BaseServerComponent {

	/**
	 * The nab
	 */
	@Value("${oauth2.server.nab}")
	private String nab;
	
	/**
	 * @return the nab as configured in the parameters
	 * @throws NotesException
	 */
	public Database getNab() throws NotesException {
		return DominoUtils.openDatabase(this.notesContext.getUserSession(), this.nab);
	}

	/**
	 * @return the nab as configured in the parameters
	 * @throws NotesException
	 */
	public synchronized Database getServerNab() throws NotesException {
		return DominoUtils.openDatabase(this.notesContext.getServerSession(), this.nab);
	}
	
	/**
	 * Return the document that correspond to a person
	 * @param userName the user name
	 * @throws NotesException
	 */
	@SuppressWarnings("unchecked")
	public Document getPersonDoc(String userName) throws NotesException {
		Vector<Database> nabs = this.notesContext.getServerSession().getAddressBooks();
		for( Database nab : nabs ) {
			if( !nab.isOpen() )
				if( !nab.open() )
					throw new RuntimeException("Server not allowed to open nab ???");
			View v = null;
			try {
				v = nab.getView("($Users)");
				Document doc = v.getDocumentByKey(userName, true);
				if( doc != null )
					return doc;
			} finally {
				DominoUtils.recycleQuietly(v);		// Recycling the view will NOT recycle the doc
			}
		}
		return null;
	}
}
