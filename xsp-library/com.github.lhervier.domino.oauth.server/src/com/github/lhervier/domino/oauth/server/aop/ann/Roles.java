package com.github.lhervier.domino.oauth.server.aop.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the roles needed to access a rest method
 * @author Lionel HERVIER
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Roles {
	public String[] roles() default "";
}
