package com.github.lhervier.domino.oauth.server.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class ReflectionUtils {

	/**
	 * Find an annotation on a method or on the parent class
	 * @param <T> the annotation type
	 * @param m the method
	 * @param clAnn the annotation class
	 * @return the annotation or null if it does not exist
	 */
	public static final <T extends Annotation> T findAnnotation(Method m, Class<T> clAnn) {
		T ann = m.getAnnotation(clAnn);
		if( ann != null )
			return ann;
		Class<?> cl = m.getDeclaringClass();
		ann = cl.getAnnotation(clAnn);
		if( ann != null )
			return ann;
		return null;
	}
}
