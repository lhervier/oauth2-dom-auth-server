package com.github.lhervier.domino.oauth.common.spring;

import com.github.lhervier.domino.oauth.common.spring.wrap.BaseWrappedDatabase;

import lotus.domino.Database;

public class DatabaseHolder {

	private BaseWrappedDatabase value;
	
	DatabaseHolder() {
		this.value = null;
	}
	
	DatabaseHolder(BaseWrappedDatabase db) {
		this.value = db;
	}
	
	void set(BaseWrappedDatabase db) {
		this.value = db;
	}
	
	public Database get() {
		if( this.value == null )
			return null;
		if( this.value.isNull() )
			return null;
		return this.value;
	}
}
