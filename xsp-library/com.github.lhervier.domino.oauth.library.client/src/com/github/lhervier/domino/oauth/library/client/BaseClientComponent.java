package com.github.lhervier.domino.oauth.library.client;

import lotus.domino.NotesException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.github.lhervier.domino.oauth.library.client.ex.OauthClientException;
import com.github.lhervier.domino.spring.servlet.NotesContext;

public abstract class BaseClientComponent {

	/**
	 * The environment
	 */
	@Autowired
	private Environment env;
	
	/**
	 * The NotesContext
	 */
	@Autowired
	private NotesContext notesContext;
	
	/**
	 * Return a property value
	 * @param name the property name
	 * @throws NotesException 
	 */
	protected String getProperty(String name) throws OauthClientException {
		try {
			// Normaly, alredy checked by the aspect...
			if( this.notesContext.getUserDatabase() == null )
				throw new RuntimeException();
			
			String propRoot = this.notesContext.getUserDatabase().getFilePath();
			propRoot = propRoot.replace('\\', '.');
			propRoot = propRoot.replace('/', '.');
			if( propRoot.endsWith(".nsf") )
				propRoot = propRoot.substring(0, propRoot.length() - ".nsf".length());
			propRoot = "oauth2.client.dbs." + propRoot;
			
			return this.env.getProperty(propRoot + "." + name);
		} catch(NotesException e) {
			throw new OauthClientException(e);
		}
	}
}
