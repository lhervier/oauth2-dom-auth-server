package com.github.lhervier.domino.oauth.server.utils;

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
				if( Utils.equals("class", name) )
					continue;
				
				// On doit avoir une méthode pour lire la valeur
				Method m = desc.getReadMethod();
				if( m == null )
					continue;
				
				// Récupère le nom de la propriété
				QueryStringName ann = m.getAnnotation(QueryStringName.class);
				String prop = ann == null ? name: ann.value();
				
				// Le nom de la propriété doit pouvoir être un paramètre
				if( !Utils.equals(URLEncoder.encode(prop, "UTF-8"), prop) )
					continue;
				
				// URL Encode la valeur (s'il y en a une)
				Object value = m.invoke(bean);
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
			throw new ServerErrorException(e);
		} catch (UnsupportedEncodingException e) {
			throw new ServerErrorException(e);
		} catch (IllegalArgumentException e) {
			throw new ServerErrorException(e);
		} catch (IllegalAccessException e) {
			throw new ServerErrorException(e);
		} catch (InvocationTargetException e) {
			throw new ServerErrorException(e);
		}
	}
}
