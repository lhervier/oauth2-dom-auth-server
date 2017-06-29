package com.github.lhervier.domino.oauth.common.spring.wrap;

import lotus.domino.Session;

import com.github.lhervier.domino.oauth.common.spring.ctx.NotesContext;

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
