package com.github.lhervier.domino.oauth.library.server.ext;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.NotesThread;
import lotus.domino.Session;

import org.springframework.stereotype.Component;

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
	 * Return the user session
	 */
	public Session getUserSession() {
		return ContextInfo.getUserSession();
	}
	
	/**
	 * Return the current database
	 */
	public Database getDatabase() {
		return ContextInfo.getUserDatabase();
	}
	
	/**
	 * Return the server session
	 */
	public Session getServerSession() {
		try {
			Session serverSession = this.serverSession.get();
			if( serverSession == null ) {
				String server = ContextInfo.getUserSession().getServerName();
				this.userNameList.set(NotesUtil.createUserNameList(server));
				serverSession = XSPNative.createXPageSession(server, this.userNameList.get(), false, false);
				this.serverSession.set(serverSession);
			}
			return serverSession;
		} catch (NotesException e) {
			throw new RuntimeException(e);
		} catch (NException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Shutdown the provider for the current thread
	 */
	public void threadShutdown() {
		Session serverSession = this.serverSession.get();
		if( serverSession != null ) {
			try {
				serverSession.recycle();
			} catch (NotesException e) {
				e.printStackTrace(System.err);
				throw new RuntimeException(e);
			}
			this.serverSession.set(null);
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
}
