package com.github.lhervier.domino.oauth.server.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.NotesPrincipal.AuthType;
import com.github.lhervier.domino.oauth.server.aop.ann.ctx.Oauth2DbContext;
import com.github.lhervier.domino.oauth.server.aop.ann.ctx.ServerRootContext;
import com.github.lhervier.domino.oauth.server.aop.ann.security.AppAuth;
import com.github.lhervier.domino.oauth.server.aop.ann.security.Bearer;
import com.github.lhervier.domino.oauth.server.aop.ann.security.Roles;
import com.github.lhervier.domino.oauth.server.aop.ann.security.UserAuth;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ex.WrongPathException;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.services.AppService;

/**
 * Check that the controller's methods are called 
 * from the oauth2 database only.
 * @author Lionel HERVIER
 */
@Component
@Aspect
public class AccessCheckAspect {

	/**
	 * Logger
	 */
	private static final Log LOG = LogFactory.getLog(AccessCheckAspect.class);
	
	/**
	 * Path to the oauth!2.nsf db
	 */
	@Value("${oauth2.server.db}")
	private String oauth2Db;
	
	/**
	 * The application service
	 */
	@Autowired
	private AppService appService;
	
	/**
	 * The user principal
	 */
	@Autowired
	private NotesPrincipal user;
	
	/**
	 * Init
	 */
	@PostConstruct
	public void init() {
		this.oauth2Db = oauth2Db.replace('\\', '/');
	}
	
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
	 * Before controller calls.
	 * FIXME: Modularize this aspect. I haven't found how to select :
	 * - methods that have a given annotation
	 * - or methods whose class have the same given annotation
	 * So, I'm extracting annotations myself...
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
		
		// If using bearer authentication, check that user is logged in
		if( this.user.getAuthType() == AuthType.BEARER ) {
			if( this.user.getName() == null ) {
				LOG.info("Accessing method '" + method.getName() + "' with an incorrect bearer token");
				throw new NotAuthorizedException();
			}
		}
		
		// Check method is executed in the context of the oauth2 database
		Oauth2DbContext o2Ctx = this.findAnnotation(method, Oauth2DbContext.class);
		if( o2Ctx != null ) {
			if( !this.oauth2Db.equals(this.user.getCurrentDatabasePath()) )
				throw new WrongPathException("oauth2 server endpoints must be called on the database declared in the oauth2.server.db property.");
		}
		
		// Check if method is called at the server root
		ServerRootContext srCtx = this.findAnnotation(method, ServerRootContext.class);
		if( srCtx != null ) {
			if( this.user.getCurrentDatabasePath() != null )
				throw new WrongPathException("This method must be called on the server root, without NSF context.");
		}
		
		// Check that we have the right roles
		Roles roles = this.findAnnotation(method, Roles.class);
		if( roles != null ) {
			for( String role : roles.roles() ) {
				if( role.length() == 0 )
					continue;
				if( !this.user.getRoles().contains("[" + role + "]") ) {
					LOG.info("User '" + this.user.getName() + "' tries to access method '" + method.getName() + "' but it does not have the required roles");
					throw new NotAuthorizedException();
				}
			}
		}
		
		// Check if we are using bearer context
		Bearer bearer = this.findAnnotation(method, Bearer.class);
		if( bearer != null ) {
			if( this.user.getAuthType() != AuthType.BEARER ) {
				LOG.info("User '" + this.user.getName() + "' tries to access method '" + method.getName() + "' but it is not authenticated with a bearer token");
				throw new NotAuthorizedException();
			}
		}
		
		// Check if method is for a user or an application
		UserAuth userAuth = this.findAnnotation(method, UserAuth.class);
		AppAuth appAuth = this.findAnnotation(method, AppAuth.class);
		if( userAuth != null || appAuth != null ) {
			Application app = this.appService.getApplicationFromName(user.getCommon());
			if( userAuth != null && app != null ) {
				LOG.info("Application '" + this.user.getName() + "' tries to access method '" + method.getName() + "', but only regular users can access it!");
				throw new NotAuthorizedException();
			}
			if( appAuth != null && app == null ) {
				LOG.info("Regular user '" + this.user.getName() + "' tries to access method '" + method.getName() + "', but only applications can access it!");
				throw new NotAuthorizedException();
			}
		}
	}
}
