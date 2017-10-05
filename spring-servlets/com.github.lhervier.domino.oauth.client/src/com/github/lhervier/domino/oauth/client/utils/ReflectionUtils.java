package com.github.lhervier.domino.oauth.client.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReflectionUtils {

	/**
	 * Pour la convertion des types primitifs en types Objets
	 */
	public final static Map<Class<?>, Class<?>> PRIMITIVES = new HashMap<Class<?>, Class<?>>();
	static {
		PRIMITIVES.put(boolean.class, Boolean.class);
		PRIMITIVES.put(byte.class, Byte.class);
		PRIMITIVES.put(short.class, Short.class);
		PRIMITIVES.put(char.class, Character.class);
		PRIMITIVES.put(int.class, Integer.class);
		PRIMITIVES.put(long.class, Long.class);
		PRIMITIVES.put(float.class, Float.class);
		PRIMITIVES.put(double.class, Double.class);
	}
	
	/**
	 * Retourne la méthode nommée. On va chercher aussi dans 
	 * les classes parentes
	 * @param cl la classe dans laquelle chercher
	 * @param m la méthode
	 * @param args ses arguments
	 * @return la méthode (ou null si elle n'existe pas).
	 */
	public static final Method getMethod(Class<?> cl, String m, Class<?>[] args) {
		try {
			return cl.getDeclaredMethod(m, args);
		} catch(NoSuchMethodException e) {
			if( cl.equals(Object.class) )
				return null;
			return getMethod(cl.getSuperclass(), m, args);
		}
	}
	
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
}
