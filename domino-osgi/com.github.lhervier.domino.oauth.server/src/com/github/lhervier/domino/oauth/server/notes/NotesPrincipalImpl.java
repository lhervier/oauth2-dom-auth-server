package com.github.lhervier.domino.oauth.server.notes;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;

import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;

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
	 * The notes context
	 */
	@Autowired
	private AuthContext authCtx;
	
	/**
	 * Return the name of the principal
	 */
	@Override
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
	@Override
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
	 * @see com.github.lhervier.domino.oauth.server.NotesPrincipal#getAuthType()
	 */
	@Override
	public AuthType getAuthType() {
		if( this.authCtx.isBearerAuth() )
			return AuthType.BEARER;
		return AuthType.NOTES;
	}

	/**
	 * Return the bearer scopes
	 */
	@Override
	public List<String> getScopes() {
		return this.authCtx.getScopes();
	}
	
	/**
	 * Return the client Id
	 */
	@Override
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

}
