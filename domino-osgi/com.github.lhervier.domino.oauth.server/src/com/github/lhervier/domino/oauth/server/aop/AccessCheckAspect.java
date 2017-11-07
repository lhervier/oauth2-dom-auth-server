package com.github.lhervier.domino.oauth.server.aop;

import java.lang.annotation.Annotation;
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

import com.github.lhervier.domino.oauth.server.BearerContext;
import com.github.lhervier.domino.oauth.server.aop.ann.ctx.Oauth2DbContext;
import com.github.lhervier.domino.oauth.server.aop.ann.ctx.ServerRootContext;
import com.github.lhervier.domino.oauth.server.aop.ann.security.Bearer;
import com.github.lhervier.domino.oauth.server.aop.ann.security.Roles;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.WrongPathException;
import com.github.lhervier.domino.spring.servlet.NotesContext;

/**
 * Check that the controller's methods are called 
 * from the oauth2 database only.
 * @author Lionel HERVIER
 */
@Component
@Aspect
public class AccessCheckAspect {

	/**
	 * The notes context
	 */
	@Autowired
	private NotesContext notesContext;
	
	/**
	 * The bearer context
	 */
	@Autowired
	private BearerContext bearerContext;
	
	/**
	 * The oauth2 db path
	 */
	@Value("${oauth2.server.db}")
	private String o2Db;
	
	/**
	 * Pointcut to detect classes we will log access
	 */
	@SuppressWarnings("unused")
	@Pointcut("within(com.github.lhervier.domino.oauth.server.controller.*)")
	private void controller() {
	}
	
	/**
	 * Find an annotation on a method or on the parent class
	 * @param <T> the annotation type
	 * @param m the method
	 * @param clAnn the annotation class
	 * @return the annotation or null if it does not exist
	 */
	private <T extends Annotation> T findAnnotation(Method m, Class<T> clAnn) {
		T ann = m.getAnnotation(clAnn);
		if( ann != null )
			return ann;
		Class<?> cl = m.getDeclaringClass();
		ann = cl.getAnnotation(clAnn);
		if( ann != null )
			return ann;
		return null;
	}
	
	/**
	 * Before controller calls
	 * @param joinPoint
	 * @throws Throwable
	 */
	@Before("controller()")
	public void checkEcriturenBefore(JoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		
		// Only check methods that have a request mapping set
		RequestMapping rm = method.getAnnotation(RequestMapping.class);
		if( rm == null )
			return;
		
		// Extract custom annotations
		Oauth2DbContext o2Ctx = this.findAnnotation(method, Oauth2DbContext.class);
		ServerRootContext srCtx = this.findAnnotation(method, ServerRootContext.class);
		Roles roles = this.findAnnotation(method, Roles.class);
		Bearer bearer = this.findAnnotation(method, Bearer.class);
		
		// Method to be called only on the oauth2 database :
		if( o2Ctx != null ) {
			
			// Check that we are on the right database
			String o2Db = this.o2Db.replace('\\', '/');
			if( this.notesContext.getUserDatabase() == null )
				throw new WrongPathException("Cannont oauth2 server endpoints on the server root. NSF context needed.");
			if( !o2Db.equals(this.notesContext.getUserDatabase().getFilePath()) )
				throw new WrongPathException("oauth2 server endpoints must be called on the database declared in the oauth2.server.db variable (" + this.o2Db + ")");
			
		// Method to be called only on the server root (no database context) :
		} else if( srCtx != null ) {
			if( this.notesContext.getUserDatabase() != null )
				throw new WrongPathException("This method must be called on the server root, without NSF context.");
		
		// Method with no annotation => Error !
		} else
			throw new RuntimeException();
		
		// Check that we have the right roles
		if( roles != null ) {
			for( String role : roles.roles() ) {
				if( role.length() == 0 )
					continue;
				if( !this.notesContext.getUserRoles().contains("[" + role + "]") )
					throw new ServerErrorException();
			}
		}
		
		// Check if we are using bearer context
		if( bearer != null ) {
			if( this.bearerContext.getBearerSession() == null )
				throw new NotAuthorizedException();
		}
	}
}
