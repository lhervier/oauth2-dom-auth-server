package com.github.lhervier.domino.oauth.server.services;

import lotus.domino.Database;
import lotus.domino.NotesException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.github.lhervier.domino.oauth.server.BaseServerComponent;

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
}
