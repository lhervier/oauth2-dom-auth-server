package com.github.lhervier.domino.oauth.common.spring.ctx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.NotesThread;
import lotus.domino.Session;

import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.ibm.domino.napi.NException;
import com.ibm.domino.napi.c.NotesUtil;
import com.ibm.domino.napi.c.Os;
import com.ibm.domino.napi.c.xsp.XSPNative;
import com.ibm.domino.osgi.core.context.ContextInfo;

@Component
public class NotesContext {

	/**
	 * The server session
	 */
	private ThreadLocal<Session> serverSession = new ThreadLocal<Session>();
	
	/**
	 * The usernamelist pointer
	 */
	private ThreadLocal<Long> userNameList = new ThreadLocal<Long>();
	
	/**
	 * The server database
	 */
	private ThreadLocal<Database> serverDatabase = new ThreadLocal<Database>();
	
	/**
	 * The user roles
	 */
	private ThreadLocal<List<String>> userRoles = new ThreadLocal<List<String>>();
	
	/**
	 * Initialisation
	 */
	public void init() {
		try {
			String server = ContextInfo.getUserSession().getServerName();
			this.userNameList.set(NotesUtil.createUserNameList(server));
			Session serverSession = XSPNative.createXPageSession(server, this.userNameList.get(), false, false);
			this.serverSession.set(serverSession);
			
			Database db = ContextInfo.getUserDatabase();
			if( db != null ) {
				Database serverDatabase = DominoUtils.openDatabase(serverSession, db.getFilePath());
				this.serverDatabase.set(serverDatabase);
			} else 
				this.serverDatabase.set(null);
			
			this.userRoles.set(new ArrayList<String>());
			if( db != null ) {
				Vector<?> roles = db.queryAccessRoles(ContextInfo.getUserSession().getUserName());
				for( Iterator<?> it = roles.iterator(); it.hasNext(); )
					this.userRoles.get().add((String) it.next());
			}
		} catch (NotesException e) {
			throw new RuntimeException(e);
		} catch (NException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Shutdown the provider for the current thread
	 */
	public void cleanUp() {
		Session serverSession = this.serverSession.get();
		if( serverSession != null ) {
			DominoUtils.recycleQuietly(serverSession);
			this.serverSession.set(null);
		}
		
		Database serverDatabase = this.serverDatabase.get();
		if( serverDatabase != null ) {
			DominoUtils.recycleQuietly(serverDatabase);
			this.serverDatabase.set(null);
		}
		
		if( this.userNameList.get() != null ) {
			try {
				Os.OSMemFree(userNameList.get());
			} catch (NException e) {
				e.printStackTrace(System.err);
				throw new RuntimeException(e);
			}
			this.userNameList.set(null);
		}
		
		NotesThread.stermThread();
	}
	
	/**
	 * Return the user session
	 */
	public Session getUserSession () {
		return ContextInfo.getUserSession();
	}
	
	/**
	 * Return the server session
	 */
	public Session getServerSession() {
		return this.serverSession.get();
	}
	
	/**
	 * Return the current database with the user rights
	 */
	public Database getUserDatabase() {
		return ContextInfo.getUserDatabase();
	}
	
	/**
	 * Return the current database opened with the server session
	 */
	public Database getServerDatabase() {
		return this.serverDatabase.get();
	}
	
	/**
	 * Return the current user roles
	 */
	public List<String> getUserRoles() {
		return this.userRoles.get();
	}
}
