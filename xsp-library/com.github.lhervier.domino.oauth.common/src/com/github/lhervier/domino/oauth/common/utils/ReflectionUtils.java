package com.github.lhervier.domino.oauth.common.utils;

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
	
	
}
