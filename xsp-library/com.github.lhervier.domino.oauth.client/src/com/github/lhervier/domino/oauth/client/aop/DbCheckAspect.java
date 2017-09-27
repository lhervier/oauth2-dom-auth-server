package com.github.lhervier.domino.oauth.client.aop;

import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.client.ex.WrongPathException;
import com.github.lhervier.domino.spring.servlet.NotesContext;

@Component
@Aspect
public class DbCheckAspect {

	/**
	 * The notes context
	 */
	@Autowired
	private NotesContext notesContext;
	
	/**
	 * The declared applications
	 */
	@Value("${oauth2.client.databases}")
	private Set<String> allowedDatabases;
	
	/**
	 * Pointcut to detect controller methods
	 */
	@SuppressWarnings("unused")
	@Pointcut("within(com.github.lhervier.domino.oauth.client.controller.*)")
	private void controller() {
	}
	
	@Before("controller()")
	public void checkDb(JoinPoint joinPoint) throws Throwable {
		// Must be on a database context
		if( this.notesContext.getUserDatabase() == null )
			throw new WrongPathException();
		
		// Database must be declared in the notes.ini
		if( !this.allowedDatabases.contains(this.notesContext.getUserDatabase().getFilePath().replace('\\', '/')) )
			throw new WrongPathException();
	}
}
