package fr.asso.afer.oauth2.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Pour serialiser/deserialiser des beans en Json
 * Contrairement à Gson, ces méthodes n'ont besoin d'aucunes dépendances
 * et 
 * @author Lionel HERVIER
 */
public class JsonUtils {

	/**
	 * Annotation pour changer le nom d'une propriété
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD})
	public static @interface SerializedName {
		String value();
	}
	
	/**
	 * Transforme une bean en Json
	 * @param b la bean
	 * @return le json
	 * @throws IntrospectionException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static final String toJson(Object b) throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Class<?> cl = b.getClass();
		
		if( cl.isAssignableFrom(String.class) )
			return stringToJson((String) b);
		
		if( cl.isAssignableFrom(Integer.class) )
			return intToJson((Integer) b);
		
		if( cl.isAssignableFrom(Long.class) )
			return longToJson((Long) b);
		
		if( cl.isAssignableFrom(Double.class) )
			return doubleToJson((Double) b);
		
		if( cl.isAssignableFrom(Date.class) )
			return dateToJson((Date) b);
		
		if( cl.isArray() ) {
			StringBuffer sb = new StringBuffer();
			sb.append("[");
			Object[] tbl = (Object[]) b;
			for( int i=0; i<tbl.length; i++ ) {
				sb.append(toJson(tbl[i]));
				if( i != tbl.length - 1 )
					sb.append(',');
			}
			sb.append("]");
			return sb.toString();
		}
		
		BeanInfo beanInfo = Introspector.getBeanInfo(cl);
		PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
		List<StringBuffer> properties = new ArrayList<StringBuffer>();
		for( int i=0; i<descriptors.length; i++ ) {
			PropertyDescriptor descriptor = descriptors[i];
			String name = descriptor.getName();
			if( "class".equals(name) )
				continue;
			Method read = descriptor.getReadMethod();
			Object value = read.invoke(b, new Object[] {});
			if( value == null )
				continue;
			
			SerializedName ann = read.getAnnotation(SerializedName.class);
			String prop;
			if( ann != null )
				prop = ann.value();
			else
				prop = name;
			
			StringBuffer sb = new StringBuffer();
			sb.append(stringToJson(prop));
			sb.append(':');
			sb.append(toJson(value));
			properties.add(sb);
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append('{');
		for( int i=0; i<properties.size(); i++ ) {
			sb.append(properties.get(i));
			if( i != properties.size() - 1 )
				sb.append(',');
		}
		sb.append('}');
		return sb.toString();
	}
	
	/**
	 * Serialise une chaîne en json
	 * @param s a chaîne à serialiser
	 * @return la chaîne serialisée
	 */
	private static final String stringToJson(String s) {
		return "\"" + s.replaceAll("\"", "\\\\\"") + "\"";
	}
	
	/**
	 * Serialise un entier en json
	 * @param i l'entier à serialiser
	 * @return l'entier serialisé
	 */
	private static final String intToJson(int i) {
		return Integer.toString(i);
	}
	
	/**
	 * Serialise un long en json
	 * @param l le long à serialiser
	 * @return le long serialisé
	 */
	private static final String longToJson(long l) {
		return Long.toString(l);
	}
	
	/**
	 * Serialise une double en json
	 * @param d le double à serialiser
	 * @return le double serialisé
	 */
	private static final String doubleToJson(double d) {
		return Double.toString(d);
	}
	
	/**
	 * Serialise une date en json
	 * @param d la date à serialiser
	 * @return la date serialisée
	 */
	private static final String dateToJson(Date d) {
		DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		return "\"" + fmt.format(d) + "\"";
	}
	
}
