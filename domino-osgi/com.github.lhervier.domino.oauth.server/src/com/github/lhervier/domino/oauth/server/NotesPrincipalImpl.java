package com.github.lhervier.domino.oauth.server;

import java.util.List;

import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.utils.DominoUtils;

/**
 * @author Lionel HERVIER
 */
@Component
public class NotesPrincipalImpl implements NotesPrincipal {

	/**
	 * Logger
	 */
	private static final Log LOG = LogFactory.getLog(NotesPrincipalImpl.class);
	
	/**
	 * The oauth2 db path
	 */
	@Value("${oauth2.server.db}")
	private String o2Db;
	
	/**
	 * The notes context
	 */
	@Autowired
	private AuthContext authCtx;
	
	/**
	 * Return the name of the principal
	 */
	public String getName() {
		Session session = this.authCtx.getUserSession();
		if( session == null )
			return null;
		try {
			return session.getEffectiveUserName();
		} catch (NotesException e) {
			LOG.error(e);
			return null;
		}
	}
	
	/**
	 * Return the common name of the user
	 * @return
	 */
	public String getCommon() {
		if( this.getName() == null )
			return null;
		
		Name nn = null;
		try {
			nn = authCtx.getServerSession().createName(this.getName());
			return nn.getCommon();
		} catch (NotesException e) {
			LOG.error(e);
			return null;
		} finally {
			DominoUtils.recycleQuietly(nn);
		}
	}
	
	/**
	 * Is the current user authenticated by notes ?
	 */
	public boolean isNotesAuth() {
		return !this.authCtx.isBearerAuth();
	}
	
	/**
	 * Is the current user authenticated using a bearer token
	 */
	public boolean isBearerAuth() {
		return this.authCtx.isBearerAuth();
	}
	
	/**
	 * Return the bearer scopes
	 */
	public List<String> getScopes() {
		return this.authCtx.getScopes();
	}
	
	/**
	 * Return the client Id
	 */
	public String getClientId() {
		return this.authCtx.getClientId();
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.NotesPrincipal#getCurrentDatabasePath()
	 */
	@Override
	public String getCurrentDatabasePath() {
		try {
			if( this.authCtx.getUserDatabase() == null )
				return null;
			return this.authCtx.getUserDatabase().getFilePath();
		} catch (NotesException e) {
			LOG.error(e);
			return null;
		}
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.NotesPrincipal#getRoles()
	 */
	@Override
	public List<String> getRoles() {
		return this.authCtx.getRoles();
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.NotesPrincipal#isOnOauth2Db()
	 */
	@Override
	public boolean isOnOauth2Db() {
		String o2Db = this.o2Db.replace('\\', '/');
		return o2Db.equals(this.getCurrentDatabasePath());
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.NotesPrincipal#isOnServerRoot()
	 */
	@Override
	public boolean isOnServerRoot() {
		return this.getCurrentDatabasePath() == null;
	}
}
