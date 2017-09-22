package com.github.lhervier.domino.oauth.library.server;

import java.util.Properties;
import java.util.Vector;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.github.lhervier.domino.spring.servlet.BaseNotesPropertySource;

public class OAuth2ServerDbPropertySource extends BaseNotesPropertySource {

	/**
	 * The properties
	 */
	private Properties props;
	
	/**
	 * Constructor
	 */
	public OAuth2ServerDbPropertySource() {
		super("oauth2-db-property-source");
	}

	/**
	 * @see com.github.lhervier.domino.spring.servlet.BaseNotesPropertySource#init(lotus.domino.Session)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void init(Session sessionAsServer) throws NotesException {
		this.props = new Properties();
		
		// Get the name of the oauth2 db from the notes.ini
		String oauth2db = sessionAsServer.getEnvironmentString("oauth2.server.db", true);
		if( oauth2db == null || oauth2db.length() == 0 )
			throw new RuntimeException("the oauth2.server.db entry must be defined in the notes.ini. It must point to the oauth2 database.");
		
		Database db = null;
		View v = null;
		Document paramDoc = null;
		try {
			db = DominoUtils.openDatabase(sessionAsServer, oauth2db);
			if( db == null )
				throw new RuntimeException("Unable to open oauth2 database (as declared in the notes.ini)");
			v = db.getView("Params");
			if( v == null )
				throw new RuntimeException("Oauth2 database does not contain the 'Params' view");
			paramDoc = v.getFirstDocument();
			if( paramDoc == null )
				return;
			
			Vector<Item> items = paramDoc.getItems();
			for( int i=0; i<items.size(); i++ ) {
				Item it = items.get(i);
				if( !it.getName().startsWith("PARAM_") )
					continue;
				String name = it.getName().substring("PARAM_".length());
				name = "oauth2.server." + name;
				this.props.setProperty(name, it.getValueString());
			}
		} finally {
			DominoUtils.recycleQuietly(paramDoc);
			DominoUtils.recycleQuietly(v);
			DominoUtils.recycleQuietly(db);
		}
		
	}

	/**
	 * @see org.springframework.core.env.PropertySource#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String name) {
		return this.props.getProperty(name);
	}

}
