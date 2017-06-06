package com.github.lhervier.domino.oauth.common.utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
}
