package com.github.lhervier.domino.oauth.server.aop.ann.ctx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Set on controllers methods to say that this method
 * can only be called when on the server root (no database context)
 * @author Lionel HERVIER
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ServerRootContext {
	
}
