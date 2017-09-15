package com.github.lhervier.domino.oauth.library.client.aop;

import lotus.domino.Database;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.library.client.BaseClientComponent;
import com.github.lhervier.domino.oauth.library.client.ex.WrongPathException;
import com.github.lhervier.domino.spring.servlet.NotesContext;

@Component
@Aspect
public class DbCheckAspect extends BaseClientComponent {

	/**
	 * The notes context
	 */
	@Autowired
	private NotesContext notesContext;
	
	/**
	 * Pointcut to detect classes we will log access
	 */
	@SuppressWarnings("unused")
	@Pointcut("within(com.github.lhervier.domino.oauth.library.client.controller.*)")
	private void controller() {
	}
	
	@Before("controller()")
	public void checkDb(JoinPoint joinPoint) throws Throwable {
		// Must be on a database context
		Database db = this.notesContext.getUserDatabase();
		if( db == null )
			throw new WrongPathException();
		
		// Must have a client id for the application
		String clientId = this.getProperty("clientId");
		if( clientId == null )
			throw new WrongPathException();
	}
}
