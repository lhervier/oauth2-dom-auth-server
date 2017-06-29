package com.github.lhervier.domino.oauth.library.server.ext.wrap;

import com.github.lhervier.domino.oauth.library.server.ext.ctx.NotesContext;

import lotus.domino.Database;

public class WrappedServerDatabase extends BaseWrappedDatabase {

	private NotesContext notesCtx;
	
	public WrappedServerDatabase(NotesContext ctx) {
		this.notesCtx = ctx;
	}
	
	@Override
	public Database getDatabase() {
		return this.notesCtx.getServerDatabase();
	}

}
