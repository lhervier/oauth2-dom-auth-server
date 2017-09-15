package com.github.lhervier.domino.oauth.library.server.services;

import javax.servlet.http.HttpServletRequest;

import lotus.domino.Database;
import lotus.domino.NotesException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.common.utils.DatabaseWrapper;
import com.github.lhervier.domino.spring.servlet.NotesContext;

@Service
public class NabService {

	/**
	 * The notes context
	 */
	@Autowired
	private NotesContext notesContext;
	
	/**
	 * The http request
	 */
	@Autowired
	private HttpServletRequest request;
	
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
		String key = this.getClass().getName() + ".nab";
		if( this.request.getAttribute(key) != null )
			return (Database) this.request.getAttribute(key);
		
		DatabaseWrapper nab = new DatabaseWrapper(this.notesContext, this.nab, false);
		this.request.setAttribute(key, nab);
		
		return nab;
	}

	/**
	 * @return the nab as configured in the parameters
	 * @throws NotesException
	 */
	public synchronized Database getServerNab() throws NotesException {
		String key = this.getClass().getName() + ".serverNab";
		if( this.request.getAttribute(key) != null )
			return (Database) this.request.getAttribute(key);
		
		DatabaseWrapper nabAsSigner = new DatabaseWrapper(this.notesContext, this.nab, true);
		this.request.setAttribute(key, nabAsSigner);
		
		return nabAsSigner;
	}
}
