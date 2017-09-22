package com.github.lhervier.domino.oauth.library.client.aop;

import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.library.client.ex.WrongPathException;
import com.github.lhervier.domino.spring.servlet.UserDatabase;

@Component
@Aspect
public class DbCheckAspect {

	/**
	 * The current database
	 */
	@Autowired
	private UserDatabase userDatabase;
	
	/**
	 * The declared applications
	 */
	@Value("${oauth2.client.databases}")
	private Set<String> allowedDatabases;
	
	/**
	 * Pointcut to detect controller methods
	 */
	@SuppressWarnings("unused")
	@Pointcut("within(com.github.lhervier.domino.oauth.library.client.controller.*)")
	private void controller() {
	}
	
	@Before("controller()")
	public void checkDb(JoinPoint joinPoint) throws Throwable {
		// Must be on a database context
		if( !this.userDatabase.isAvailable() )
			throw new WrongPathException();
		
		// Database must be declared in the notes.ini
		if( !this.allowedDatabases.contains(this.userDatabase.getFilePath().replace('\\', '/')) )
			throw new WrongPathException();
	}
}
