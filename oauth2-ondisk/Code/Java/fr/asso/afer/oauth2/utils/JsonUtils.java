package fr.asso.afer.oauth2.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
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
	public static @interface JsonName {
		String value();
	}
	
	/**
	 * Exception en cas d'erreur pendant la serialisation
	 */
	public static class JsonSerializeException extends Exception {

		private static final long serialVersionUID = -8700800053524239818L;

		public JsonSerializeException() {
			super();
		}

		public JsonSerializeException(String message) {
			super(message);
		}

		public JsonSerializeException(String message, Throwable cause) {
			super(message, cause);
		}

		public JsonSerializeException(Throwable cause) {
			super(cause);
		}
		
	}
	
	/**
	 * Transforme une bean en Json
	 * @param b la bean
	 * @return le json
	 * @throws JsonSerializeException
	 */
	public static final String toJson(Object b) throws JsonSerializeException {
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
		
		try {
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
				
				JsonName ann = read.getAnnotation(JsonName.class);
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
		} catch (IntrospectionException e) {
			throw new JsonSerializeException(e);
		} catch (IllegalArgumentException e) {
			throw new JsonSerializeException(e);
		} catch (IllegalAccessException e) {
			throw new JsonSerializeException(e);
		} catch (InvocationTargetException e) {
			throw new JsonSerializeException(e);
		}
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
	
	// ==============================================================================
	
	public static class JsonDeserializeException extends Exception {

		private static final long serialVersionUID = -2539976858976251481L;

		public JsonDeserializeException() {
			super();
		}

		public JsonDeserializeException(String message) {
			super(message);
		}

		public JsonDeserializeException(String message, Throwable cause) {
			super(message, cause);
		}

		public JsonDeserializeException(Throwable cause) {
			super(cause);
		}
		
	}
	
	private static class JsonParser {
		private int pos = 0;
		private String json;
		
		public JsonParser(String json) {
			this.pos = 0;
			this.json = json;
		}
		
		public void next() {
			this.pos++;
		}
		
		public char element() {
			return this.json.charAt(this.pos);
		}
		
		public boolean eof() {
			return this.pos > this.json.length() - 1;
		}
		
		public void forward() {
			if( this.eof() )
				return;
			
			char el = this.element();
			if( el != ' ' && el != '\t' && el != '\n' && el != '\r' )
				return;
			
			this.next();
			forward();
		}
	}
	
	/**
	 * Transforme une chaîne Json en un objet
	 * @param json la chaîne Json
	 * @param cl l'objet à retourner
	 * @return une instance de l'objet
	 * @throws JsonDeserializeException 
	 */
	public static final <T> T fromJson(String json, Class<T> cl) throws JsonDeserializeException {
		return fromJson(new JsonParser(json), cl);
	}
	
	/**
	 * Transforme une chaîne Json en un objet
	 * @param json la chaîne Json
	 * @param cl l'objet à retourner
	 * @return une instance de l'objet
	 * @throws JsonDeserializeException 
	 */
	@SuppressWarnings("unchecked")
	private static final <T> T fromJson(JsonParser json, Class<T> cl) throws JsonDeserializeException {
		if( cl.isAssignableFrom(String.class) )
			return (T) stringFromJson(json);
		
		if( cl.isAssignableFrom(Integer.class) || cl.isAssignableFrom(int.class) )
			return (T) intFromJson(json);
		
		if( cl.isAssignableFrom(Long.class) || cl.isAssignableFrom(long.class) )
			return (T) longFromJson(json);
		
		if( cl.isAssignableFrom(Double.class) || cl.isAssignableFrom(double.class) )
			return (T) doubleFromJson(json);
		
		if( cl.isAssignableFrom(Date.class) )
			return (T) dateFromJson(json);
		
		if( cl.isAssignableFrom(Boolean.class) || cl.isAssignableFrom(boolean.class) )
			return (T) boolFromJson(json);
		
		if( cl.isAssignableFrom(List.class) )
			return (T) listFromJson(json, cl);
		
		if( cl.isArray() )
			return (T) arrayFromJson(json, cl.getComponentType());
		
		return (T) objectFromJson(json, cl);
	}
	
	/**
	 * Récupère une chaîne
	 * @param json le json
	 * @return la chaîne
	 */
	private static final String stringFromJson(JsonParser json) throws JsonDeserializeException {
		json.forward();
		
		char el = json.element();
		if( el != '"' )
			throw new JsonDeserializeException("Json incorrect...");
		
		json.next();
		StringBuffer sb = new StringBuffer();
		while( json.element() != '"' ) {
			if( json.element() == '\\' ) {
				json.next();
			}
			sb.append(json.element());
			json.next();
		}
		json.next();
		return sb.toString();
	}
	
	/**
	 * Récupère un booleen
	 * @return le booleen
	 */
	private static final Boolean boolFromJson(JsonParser json) throws JsonDeserializeException {
		json.forward();
		
		String s = "";
		for( int i=0; i<4; i++ ) {
			s += json.element();
			json.next();
		}
		if( !"true".equals(s) && !"false".equals(s) )
			throw new JsonDeserializeException("Json incorrect");
		return Boolean.parseBoolean(s);
	}
	
	/**
	 * Récupère un entier
	 * @param json le json
	 * @return l'entier
	 */
	private static final Integer intFromJson(JsonParser json) throws JsonDeserializeException {
		json.forward();
		
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		while( !json.eof() && ((json.element() >= '0' && json.element() < '9') || json.element() == '-') ) {
			if( !first && json.element() == '-' )
				throw new JsonDeserializeException("Json incorrect");
			sb.append(json.element());
			json.next();
			first = false;
		}
		return Integer.parseInt(sb.toString());
	}
	
	/**
	 * Récupère un long
	 * @param json le json
	 * @return le long
	 */
	private static final Long longFromJson(JsonParser json) throws JsonDeserializeException {
		json.forward();
		
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		while( !json.eof() && ((json.element() >= '0' && json.element() <= '9') || json.element() == '-') ) {
			if( !first && json.element() == '-' )
				throw new JsonDeserializeException("Json incorrect");
			sb.append(json.element());
			json.next();
			first = false;
		}
		return Long.parseLong(sb.toString());
	}
	
	/**
	 * Récupère un double
	 * @param json le json
	 * @return le double
	 */
	private static final Double doubleFromJson(JsonParser json) throws JsonDeserializeException {
		json.forward();
		
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		while( !json.eof() && ((json.element() >= '0' && json.element() < '9') || json.element() == '-' || json.element() == '.' ) ) {
			if( !first && json.element() == '-' )
				throw new JsonDeserializeException("Json incorrect");
			sb.append(json.element());
			json.next();
			first = false;
		}
		return Double.parseDouble(sb.toString());
	}
	
	/**
	 * Récupère une date
	 * @param json le json
	 * @return la date
	 */
	private static final Date dateFromJson(JsonParser json) throws JsonDeserializeException {
		json.forward();
		
		String dt = stringFromJson(json);
		DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		try {
			return fmt.parse(dt);
		} catch (ParseException e) {
			throw new JsonDeserializeException("Json incorrect", e);
		}
	}
	
	/**
	 * Retourne une liste depuis un json
	 * @param json le json
	 * @param cl la classe
	 * @throws JsonDeserializeException 
	 */
	private static final <T> List<T> listFromJson(JsonParser json, Class<T> cl) throws JsonDeserializeException {
		json.forward();
		char el = json.element();
		if( el != '[' )
			throw new JsonDeserializeException("Json incorrect");
		json.next();
		List<T> ret = new ArrayList<T>();
		while( json.element() != ']' ) {
			T obj = fromJson(json, cl);
			ret.add(obj);
			json.forward();
			if( json.element() == ',' ) {
				json.next();
				json.forward();
			}
		}
		return ret;
	}
	
	/**
	 * Retourne un tableau depuis un json
	 * @param json le json
	 * @param cl la classe
	 * @return le tableau
	 * @throws JsonDeserializeException 
	 */
	@SuppressWarnings("unchecked")
	private static final <T> T[] arrayFromJson(JsonParser json, Class<T> cl) throws JsonDeserializeException {
		List<T> ret = listFromJson(json, cl);
		T[] tbl = (T[]) Array.newInstance(cl, ret.size());
		ret.toArray(tbl);
		return tbl;
	}
	
	/**
	 * Retourne une objet à partir d'un json
	 * @param json le json
	 * @param cl la classe à retourner
	 * @return l'objet
	 * @throws JsonDeserializeException 
	 */
	private static final <T> T objectFromJson(JsonParser json, Class<T> cl) throws JsonDeserializeException {
		json.forward();
		if( json.element() != '{' )
			throw new JsonDeserializeException("Json incorrect");
		json.next();
		
		try {
			T ret = cl.newInstance();
			while( json.element() != '}' ) {
				String prop = stringFromJson(json);
				json.forward();
				if( json.element() != ':' )
					throw new JsonDeserializeException("Json incorrect");
				json.next();
				
				Class<?> type = ReflectionUtils.getPropType(cl, prop);
				Object value = fromJson(json, type);
				Method wrt = ReflectionUtils.getWriteMethod(cl, prop);
				wrt.invoke(ret, new Object[] {value});
				
				json.forward();
				if( json.element() == ',' )
					json.next();
			}
			return ret;
		} catch (IllegalAccessException e) {
			throw new JsonDeserializeException(e);
		} catch (InstantiationException e) {
			throw new JsonDeserializeException(e);
		} catch (IntrospectionException e) {
			throw new JsonDeserializeException(e);
		} catch (IllegalArgumentException e) {
			throw new JsonDeserializeException(e);
		} catch (InvocationTargetException e) {
			throw new JsonDeserializeException(e);
		}
	}
}
