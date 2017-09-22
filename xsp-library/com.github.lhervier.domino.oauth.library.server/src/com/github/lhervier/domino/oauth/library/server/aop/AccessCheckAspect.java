package com.github.lhervier.domino.oauth.library.server.aop;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.lhervier.domino.oauth.library.server.aop.ann.Oauth2DbContext;
import com.github.lhervier.domino.oauth.library.server.aop.ann.Roles;
import com.github.lhervier.domino.oauth.library.server.aop.ann.ServerRootContext;
import com.github.lhervier.domino.oauth.library.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.library.server.ex.WrongPathException;
import com.github.lhervier.domino.spring.servlet.UserDatabase;
import com.github.lhervier.domino.spring.servlet.UserRoles;

/**
 * Check that the controller's methods are called 
 * from the oauth2 database only.
 * @author Lionel HERVIER
 */
@Component
@Aspect
public class AccessCheckAspect {

	/**
	 * The current database opened as the current user
	 */
	@Autowired
	private UserDatabase userDatabase;
	
	/**
	 * The current user roles
	 */
	@Autowired
	private UserRoles userRoles;
	
	/**
	 * The oauth2 db path
	 */
	@Value("${oauth2.server.db}")
	private String o2Db;
	
	/**
	 * Pointcut to detect classes we will log access
	 */
	@SuppressWarnings("unused")
	@Pointcut("within(com.github.lhervier.domino.oauth.library.server.controller.*)")
	private void controller() {
	}
	
	@Before("controller()")
	public void checkEcriturenBefore(JoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		
		// Only check methods that have a request mapping set
		RequestMapping rm = method.getAnnotation(RequestMapping.class);
		if( rm == null )
			return;
		
		// Extract custom annotations
		Oauth2DbContext o2Ctx = method.getAnnotation(Oauth2DbContext.class);
		ServerRootContext srCtx = method.getAnnotation(ServerRootContext.class);
		Roles roles = method.getAnnotation(Roles.class);
		
		// Method to be called only on the oauth2 database :
		if( o2Ctx != null ) {
			
			// Check that we are on the right database
			String o2Db = this.o2Db.replace('\\', '/');
			if( this.userDatabase == null )
				throw new WrongPathException();
			if( !o2Db.equals(this.userDatabase.getFilePath()) )
				throw new WrongPathException();
			
		// Method to be called only on the server root (no database context) :
		} else if( srCtx != null ) {
			if( this.userDatabase.isAvailable() )
				throw new WrongPathException();
		
		// Method with no annotation => Error !
		} else
			throw new RuntimeException();
		
		// Check that we have the right roles
		if( roles != null ) {
			for( String role : roles.roles() ) {
				if( role.length() == 0 )
					continue;
				if( !this.userRoles.contains("[" + role + "]") )
					throw new ServerErrorException();
			}
		}
	}
}
