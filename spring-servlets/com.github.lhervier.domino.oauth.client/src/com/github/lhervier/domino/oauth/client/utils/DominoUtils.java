package com.github.lhervier.domino.oauth.client.utils;

import java.lang.reflect.InvocationTargetException;

import lotus.domino.NotesException;
import lotus.domino.Session;

/**
 * Méthodes pratiques pour Domino
 * @author Lionel HERVIER
 */
public class DominoUtils {

	/**
	 * Return a notes ini variable
	 * @param session the session
	 * @param var the name of the variable to retrieve
	 * @param cl the type to return (will invoke a constructor that takes a single string parameter)
	 * @param nullValue to return if the variable does not exists
	 */
	public static final <T> T getEnvironment(Session session, String var, Class<T> cl, T nullValue) {
		try {
			String value = session.getEnvironmentString(var, true);
			if( value == null )
				return nullValue;
			return (T) cl.getConstructor(String.class).newInstance(new Object[] {value});
		} catch (IllegalArgumentException e) {
			return nullValue;
		} catch (SecurityException e) {
			return nullValue;
		} catch (InstantiationException e) {
			return nullValue;
		} catch (IllegalAccessException e) {
			return nullValue;
		} catch (InvocationTargetException e) {
			return nullValue;
		} catch (NoSuchMethodException e) {
			return nullValue;
		} catch (NotesException e) {
			return nullValue;
		}
	}
}
