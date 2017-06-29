package com.github.lhervier.domino.oauth.common.spring.wrap;

import lotus.domino.Database;

import com.github.lhervier.domino.oauth.common.spring.ctx.NotesContext;

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
