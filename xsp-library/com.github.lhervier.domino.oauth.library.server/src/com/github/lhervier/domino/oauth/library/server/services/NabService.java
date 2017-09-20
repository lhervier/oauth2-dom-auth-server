package com.github.lhervier.domino.oauth.library.server.services;

import lotus.domino.Database;
import lotus.domino.NotesException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.github.lhervier.domino.oauth.library.server.BaseServerComponent;
import com.github.lhervier.domino.spring.servlet.ServerSession;
import com.github.lhervier.domino.spring.servlet.UserSession;

@Service
public class NabService extends BaseServerComponent {

	/**
	 * The session opened as the user
	 */
	@Autowired
	private UserSession userSession;
	
	/**
	 * The session opened as the server
	 */
	@Autowired
	private ServerSession serverSession;
	
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
		return DominoUtils.openDatabase(this.userSession, this.nab);
	}

	/**
	 * @return the nab as configured in the parameters
	 * @throws NotesException
	 */
	public synchronized Database getServerNab() throws NotesException {
		return DominoUtils.openDatabase(this.serverSession, this.nab);
	}
}
