package com.github.lhervier.domino.oauth.client.utils;

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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Méthode utiles pour gérer les utls
 * @author Lionel HERVIER
 */
public class QueryStringUtils {
	
	/**
	 * Annotation pour la serialisation
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD})
	public static @interface QueryStringName {
		String value();
	}
	
	/**
	 * Serialise une bean dans une url
	 * @param baseUri l'url de base
	 * @param b la bean
	 * @return le query string
	 */
	public static final <T> String addBeanToQueryString(String baseUri, T bean) {
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
			PropertyDescriptor[] descs = beanInfo.getPropertyDescriptors();
			List<String> params = new ArrayList<String>();
			for( PropertyDescriptor desc : descs ) {
				String name = desc.getName();
				
				// On filtre la propriété "class"
				if( "class".equals(name) )
					continue;
				
				// On doit avoir une méthode pour lire la valeur
				Method m = desc.getReadMethod();
				if( m == null )
					continue;
				
				// Récupère le nom de la propriété
				QueryStringName ann = m.getAnnotation(QueryStringName.class);
				String prop = ann == null ? name: ann.value();
				
				// Le nom de la propriété doit pouvoir être un paramètre
				if( !URLEncoder.encode(prop, "UTF-8").equals(prop) )
					continue;
				
				// URL Encode la valeur (s'il y en a une)
				Object value = m.invoke(bean, new Object[] {});
				if( value != null )
					params.add(prop + '=' + URLEncoder.encode(value.toString(), "UTF-8"));
			}
			
			// Serialise tout dans l'uri de base
			StringBuffer sb = new StringBuffer();
			sb.append(baseUri);
			if( baseUri.indexOf('?') == -1 )
				sb.append('?');
			else
				sb.append('&');
			for( int i=0; i<params.size(); i++ ) {
				sb.append(params.get(i));
				if( i != params.size() - 1 )
					sb.append('&');
			}
			return sb.toString();
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
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
	 * Créé une bean à partir du queryString
	 * @param request the http request
	 * @param cl la classe de la bean à créer
	 * @return la bean
	 */
	public static final <T> T createBean(HttpServletRequest request, Class<T> cl) {
		try {
			T ret = cl.newInstance();
			
			BeanInfo beanInfo = Introspector.getBeanInfo(cl);
			PropertyDescriptor[] descs = beanInfo.getPropertyDescriptors();
			for( PropertyDescriptor desc : descs ) {
				String name = desc.getName();
				if( "class".equals(name) )
					continue;
				
				if( StringUtils.isEmpty(request.getParameter(name)) )
					continue;
				
				Class<?> t = desc.getWriteMethod().getParameterTypes()[0];
				if( t.isPrimitive() )
					t = ReflectionUtils.PRIMITIVES.get(t);
				
				String prop;
				if( desc.getReadMethod() != null ) {
					QueryStringName ann = desc.getReadMethod().getAnnotation(QueryStringName.class);
					if( ann != null ) {
						prop = ann.value();
					} else 
						prop = name;
				} else
					prop = name;
				
				
				Object value;
				if( !t.isAssignableFrom(List.class) )
					value = t.getConstructor(String.class).newInstance(request.getParameter(prop));
				else {
					List<Object> lst = new ArrayList<Object>();
					String[] values = request.getParameter(prop).split(",");
					for( String v : values )
						lst.add(v);
					value = lst;
				}
				
				desc.getWriteMethod().invoke(ret, new Object[] {value});
			}
			return ret;
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}
