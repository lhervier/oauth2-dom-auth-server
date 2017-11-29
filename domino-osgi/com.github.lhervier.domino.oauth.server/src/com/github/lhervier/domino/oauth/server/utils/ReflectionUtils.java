package com.github.lhervier.domino.oauth.server.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class ReflectionUtils {

	/**
	 * Copy a set of properties from one bean to another
	 * @param src the source bean
	 * @param dest the dest bean
	 * @param props the properties to copy
	 */
	public static final <T> void copyProperties(T src, T dest, String[] props) {
		if( src == null || dest == null )
			throw new RuntimeException("source and dest objects cannot be null...");
		
		Set<String> sProps = new HashSet<String>();
		for( String prop : props )
			sProps.add(prop);
		
		try {
			Class<?> cl = src.getClass();
			BeanInfo beanInfo = Introspector.getBeanInfo(cl);
			PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
			for( PropertyDescriptor desc : descriptors ) {
				if( desc.getReadMethod() == null || desc.getWriteMethod() == null )
					continue;
				if( !sProps.contains(desc.getName()) )
					continue;
				desc.getWriteMethod().invoke(dest, new Object[] {desc.getReadMethod().invoke(src, new Object[] {})});
			}
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Find an annotation on a method or on the parent class
	 * @param <T> the annotation type
	 * @param m the method
	 * @param clAnn the annotation class
	 * @return the annotation or null if it does not exist
	 */
	public final static <T extends Annotation> T findAnnotation(Method m, Class<T> clAnn) {
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
