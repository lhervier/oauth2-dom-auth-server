package com.github.lhervier.domino.oauth.library.server.ext;

import lotus.domino.Session;

public class WrappedServerSession extends BaseWrappedSession {

	private NotesContext notesCtx;
	
	public WrappedServerSession(NotesContext ctx) {
		this.notesCtx = ctx;
	}
	
	@Override
	public Session getSession() {
		return this.notesCtx.getServerSession();
	}

}
