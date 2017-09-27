package com.github.lhervier.domino.oauth.client;

import java.util.HashSet;
import java.util.Set;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

import com.github.lhervier.domino.spring.servlet.BaseParamViewPropertySource;

public class Oauth2ClientDbPropertySource extends BaseParamViewPropertySource {

	/**
	 * Authorized databases
	 */
	private Set<String> databases = new HashSet<String>();
	
	/**
	 * Constructor
	 */
	public Oauth2ClientDbPropertySource() {
		super("oauth2-client-property-source");
	}

	/**
	 * Return the name of the view that contains the parameters
	 */
	@Override
	protected String getViewName() {
		return "Oauth2Params";
	}
	
	/**
	 * @see com.github.lhervier.domino.spring.servlet.BaseParamViewPropertySource#getPrefix()
	 */
	@Override
	protected String getPrefix() {
		return "oauth2.client.";
	}

	/**
	 * @see com.github.lhervier.domino.spring.servlet.BaseParamViewPropertySource#init(lotus.domino.Session)
	 */
	@Override
	public void init(Session sessionAsServer) throws NotesException {
		super.init(sessionAsServer);
		
		String dbs = sessionAsServer.getEnvironmentString("oauth2.client.databases", true);
		if( dbs == null || dbs.length() == 0 )
			throw new RuntimeException("The 'oauth2.client.databases' must be defined in the notes.ini. It must contains the names of the oauth2 client databases.");
		String[] tblDbs = dbs.split(",");
		for( String db : tblDbs )
			this.databases.add(db.replace('\\', '/').trim());
	}

	/**
	 * Check if the database is elligible to extract properties.
	 * OAUTH2 Client database must be declared in the notes.ini
	 */
	@Override
	public boolean checkDb(Database database) throws NotesException {
		return this.databases.contains(database.getFilePath().replace('\\', '/'));
	}
}
