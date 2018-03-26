package com.github.lhervier.domino.oauth.server.utils;

import static java.net.URLEncoder.encode;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;

/**
 * Méthode utiles pour gérer les utls
 * @author Lionel HERVIER
 */
public class QueryStringUtils {
	
	/**
	 * Private constructor
	 */
	private QueryStringUtils() {
	}
	
	/**
	 * Annotation pour la serialisation
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD})
	public static @interface QueryStringName {
		String value();
	}
	
	/**
	 * Return the value of a property
	 * @param desc the property descriptor
	 * @param bean the object
	 * @return the value
	 */
	private static final String getPropertyValue(PropertyDescriptor desc, Object bean) {
		try {
			String name = desc.getName();
			
			// No value for "class" property
			if( Utils.equals("class", name) )
				return null;
			
			// No value if no read method
			Method m = desc.getReadMethod();
			if( m == null )
				return null;
			
			// Return the value as String
			Object value = m.invoke(bean);
			return value == null ? null : value.toString();
		} catch (IllegalAccessException e) {
			throw new ServerErrorException(e);
		} catch (IllegalArgumentException e) {
			throw new ServerErrorException(e);
		} catch (InvocationTargetException e) {
			throw new ServerErrorException(e);
		}
	}
	
	/**
	 * Serialise une bean dans une url
	 * @param baseUri l'url de base
	 * @param b la bean
	 * @return le query string
	 */
	public static final <T> String addBeanToQueryString(String baseUri, T bean) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(baseUri);
			char sep = baseUri.indexOf('?') == -1 ? '?' : '&';
			
			BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
			PropertyDescriptor[] descs = beanInfo.getPropertyDescriptors();
			for( PropertyDescriptor desc : descs ) {
				String value = getPropertyValue(desc, bean);
				if( value == null )
					continue;
				
				QueryStringName ann = desc.getReadMethod().getAnnotation(QueryStringName.class);
				String prop = ann == null ? desc.getName(): ann.value();
				
				if( Utils.equals(encode(prop, "UTF-8"), prop) ) {
					sb.append(sep).append(prop).append('=').append(encode(value, "UTF-8"));
					sep = '&';
				}
			}
			
			return sb.toString();
		} catch (IntrospectionException e) {
			throw new ServerErrorException(e);
		} catch (UnsupportedEncodingException e) {
			throw new ServerErrorException(e);
		}
	}
}
