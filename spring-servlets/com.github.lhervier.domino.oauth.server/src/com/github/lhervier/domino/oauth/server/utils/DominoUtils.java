package com.github.lhervier.domino.oauth.server.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import lotus.domino.Base;
import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.NotesException;
import lotus.domino.RichTextItem;
import lotus.domino.Session;
import lotus.domino.View;

/**
 * Méthodes pratiques pour Domino
 * @author Lionel HERVIER
 */
public class DominoUtils {

	/**
	 * La liste des objets supportés pour stocker dans un champ
	 */
	private final static Set<Class<?>> supported = new HashSet<Class<?>>();
	static {
		supported.add(String.class);
		supported.add(int.class);
		supported.add(Integer.class);
		supported.add(double.class);
		supported.add(Double.class);
		supported.add(Long.class);
		supported.add(long.class);
		supported.add(Date.class);
		supported.add(boolean.class);
		supported.add(Boolean.class);
		supported.add(Vector.class);
		supported.add(List.class);
	}
	
	/**
	 * Pour savoir si un type est supporté
	 * @param cl le type à vérifier
	 * @return true si le type est supporté. False sinon
	 */
	private static final boolean isSupported(Class<?> cl) {
		boolean supp = false;
		for( Class<?> suppCl : supported ) {
			if( cl.isAssignableFrom(suppCl) ) {
				supp = true;
				break;
			}
		}
		return supp;
	}
	
	/**
	 * Pour recycler un object Domino
	 * @param o l'objet à recycler
	 */
	public static final void recycleQuietly(Base o) {
		if( o == null )
			return;
		try {
			o.recycle();
		} catch(NotesException e) {
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * Pour recycler une liste d'objects Domino
	 * @param v les objets à recycler
	 */
	public static final void recycleQuietly(List<? extends Base> o) {
		if( o == null )
			return;
		for( Base b : o )
			DominoUtils.recycleQuietly(b);
	}
	
	/**
	 * Ouvre une database
	 * @param session la session pour ouvrir la base
	 * @param filePath le chemin vers la base
	 * @return la database ou null si elle n'existe pas
	 * @throws NotesException si on n'a pas les droits
	 */
	public static final Database openDatabase(Session session, String name) throws NotesException {
		Database db;
		try {
			db = session.getDatabase(null, name, false);		// createOnFail = false
		} catch(NotesException e) {
			return null;
		}
		if( db == null )
			return null;
		if( !db.isOpen() )
			if( !db.open() ) 
				return null;
		return db;
	}
	
	/**
	 * Retourne la liste des valeurs d'un champ
	 * @param doc le document
	 * @param field le nom du champ
	 * @param cl le type d'objet que l'on attend
	 * @return la liste des valeurs
	 * @throws NotesException en cas de pb
	 */
	@SuppressWarnings("unchecked")
	public static final <T> List<T> getItemValue(Document doc, String field, Class<T> cl) throws NotesException {
		List<T> ret = new ArrayList<T>();
		Vector<T> values = doc.getItemValue(field);
		if( values == null )
			return ret;
		ret.addAll(values);
		return ret;
	}
	
	/**
	 * Remplace la valeur d'un champ multiple
	 * @param doc le document
	 * @param field le nom du champ
	 * @param values les valeurs sous forme d'une liste
	 * @throws NotesException en cas de pb
	 */
	public static final <T> void replaceItemValue(Document doc, String field, List<T> values) throws NotesException {
		Vector<T> v = new Vector<T>();
		if( values != null )
			v.addAll(values);
		doc.replaceItemValue(field, v);
	}
	
	/**
	 * Retourne une vue en s'assurant qu'elle existe bien
	 * @param db le nom de la base
	 * @param name le nom de la vue
	 * @return la vue
	 * @throws NotesException en cas de pb
	 */
	public static final View getView(Database db, String name) throws NotesException {
		View v = db.getView(name);
		if( v == null )
			throw new RuntimeException("Je ne trouve pas la vue '" + name + "' dans la base '" + db + "'");
		v.setAutoUpdate(false);
		return v;
	}
	
	/**
	 * Pour enregistrer un document en forcant un computeWithForm
	 * @param doc le doc à sauver
	 * @throws NotesException en cas de pb
	 */
	public static final void computeAndSave(Document doc) throws NotesException {
		if( !doc.computeWithForm(true, true) )
			throw new RuntimeException("Erreur pendant le computeWithForm");
		doc.save(true, false);
	}
	
	/**
	 * Attend que la tâche updall ai terminé
	 * @param session la session Notes
	 */
	public static final void waitForUpdall(Session session) throws NotesException {
		String tasks = session.sendConsoleCommand(session.getServerName(), "sh ta");
		while( tasks.indexOf("Index All") != -1 ) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			tasks = session.sendConsoleCommand(session.getServerName(), "sh ta");
		}
	}
	
	/**
	 * Pour forcer un rafraîchissement du NAB
	 * @param nab la nab à rafraîchir
	 * @throws NotesException en cas de pb
	 */
	public static final void refreshNab(Database nab) throws NotesException {
		Session session = nab.getParent();
		
		session.sendConsoleCommand(session.getServerName(), "load updall " + nab.getFilePath() + " -t \"($ServerAccess)\"");
		waitForUpdall(session);
		
		session.sendConsoleCommand(session.getServerName(), "load updall " + nab.getFilePath() + " -t \"($Users)\"");
		waitForUpdall(session);
		
		session.sendConsoleCommand(session.getServerName(), "dbcache flush");
		session.sendConsoleCommand(session.getServerName(), "show nlcache reset");
	}
	
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
	
	/**
	 * Rempli un objet à partir d'un document Notes
	 * @param o l'objet à remplir
	 * @param doc le document qui contient les champs
	 * @param prefix le préfixe des champs
	 * @return l'objet rempli
	 * @throws NotesException en cas de problème
	 */
	public final static <T> T fillObject(T o, Document doc) throws NotesException {
		return fillObject(o, doc, "", null);
	}
	public final static <T> T fillObject(T o, Document doc, String prefix) throws NotesException {
		return fillObject(o, doc, prefix, null);
	}
	public final static <T> T fillObject(T o, Document doc, String prefix, DateFormat fmt) throws NotesException {
		try {
			// On l'introspecte pour ne pas mettre les champs en dur
			Class<?> cl = o.getClass();
			BeanInfo beanInfo = Introspector.getBeanInfo(cl);
			PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
			
			// Parcours les propriétés de l'objet
			for( int i=0; i<descriptors.length; i++ ) {
				PropertyDescriptor descriptor = descriptors[i];
				
				// On ne tient pas compte de "class"
				if( "class".equals(descriptor.getName()) )
					continue;
				
				// On vérifie que le document ait un champ correspondant
				String name = prefix + descriptor.getName();
				if( !doc.hasItem(name) )
					continue;
				
				// Récupère le type du champ: Attention, il faut convertir les types primitifs
				Method setter = descriptor.getWriteMethod();
				if( setter == null )
					continue;
				Class<?>[] parameters = setter.getParameterTypes();
				if( parameters.length != 1 )
					continue;			// Un setter avec 2 paramètres ???
				Class<?> paramClass;
				if( parameters[0].isPrimitive() )
					paramClass = ReflectionUtils.PRIMITIVES.get(parameters[0]);
				else
					paramClass = parameters[0];
				
				// En fonction du type, on appel une méthode ou une autre
				
				// Chaîne de caractère: Attention aux Rich Text
				if( paramClass.isAssignableFrom(String.class) ) {
					Item it = doc.getFirstItem(name);
					String v;
					if( it.getType() == Item.RICHTEXT )
						v = ((RichTextItem) it).getUnformattedText();
					else
						v = doc.getItemValueString(name);
					setter.invoke(o, new Object[] {v});
				
				// Nombre
				} else if( paramClass.isAssignableFrom(Integer.class) )
					setter.invoke(o, new Object[] {doc.getItemValueInteger(name)});
				else if( paramClass.isAssignableFrom(Double.class) )
					setter.invoke(o, new Object[] {doc.getItemValueDouble(name)});
				else if( paramClass.isAssignableFrom(Long.class) )
					setter.invoke(o, new Object[] {(long) doc.getItemValueDouble(name)});
				
				// Boolean: On regarde la valeur "1"
				else if( paramClass.isAssignableFrom(Boolean.class) )
					setter.invoke(o, new Object[] {"1".equals(doc.getItemValueString(name))});
				
				// Un champ multi valué => Attention aux DateTime qu'on converti en dates java 
				else if( paramClass.isAssignableFrom(List.class) ) {
					List<Object> values;
					
					Item it = doc.getFirstItem(name);
					if( it.getType() != Item.DATETIMES )
						values = getItemValue(doc, name, Object.class);
					else {
						values = new ArrayList<Object>();
						List<DateTime> dts = getItemValue(doc, name, DateTime.class);
						for( Iterator<DateTime> iterator = dts.iterator(); iterator.hasNext(); ) {
							DateTime dt = iterator.next();
							values.add(dt.toJavaDate());
						}
						DominoUtils.recycleQuietly(dts);
					}
					
					setter.invoke(o, new Object[] {values});
					
				// DateTime: On converti en date java
				} else if( paramClass.isAssignableFrom(Date.class) ) {
					Date dt = null;
					
					// Un vrai champ date
					Item it = doc.getFirstItem(name);
					if( it.getType() == Item.DATETIMES ) {
						List<DateTime> values = getItemValue(doc, name, DateTime.class);
						if( values != null && values.size() != 0 )
							dt = values.get(0).toJavaDate();
						
					// Un champ texte qu'on va interprêter en date
					} else if( it.getType() == Item.TEXT ) {
						String s = doc.getItemValueString(name);
						try {
							dt = fmt.parse(s);
						} catch(ParseException e) {
						}
					}
					setter.invoke(o, new Object[] {dt});
				
				// Sinon, on utilise un constructeur depuis une chaîne
				} else if( supported.contains(paramClass) ) {
					String v = doc.getItemValueString(name);
					Constructor<?> c = paramClass.getConstructor(String.class);
					setter.invoke(o, new Object[] {c.newInstance(v)});
				}
			}
			
			// Retourne l'objet
			return o;
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
	}
	
	// ===========================================================================================
	
	/**
	 * Rempli un document à partir d'une bean.
	 * On créé un champ par propriété.
	 * @param doc le document à remplir
	 * @param o la bean d'où extraire les propriétés
	 * @throws NotesException en cas de problème
	 */
	public final static void fillDocument(Document doc, Object o) throws NotesException {
		fillDocument(doc, o, null, null, null);
	}
	
	/**
	 * Rempli un document à partir d'une bean.
	 * On créé un champ par propriété.
	 * @param doc le document à remplir
	 * @param o la bean d'où extraire les propriétés
	 * @param prefix un préfixe à ajouter devant les noms de champs
	 * @throws NotesException en cas de problème
	 */
	public final static void fillDocument(Document doc, Object o, String prefix) throws NotesException {
		fillDocument(doc, o, prefix, null, null);
	}
	
	/**
	 * Rempli un document à partir d'une bean.
	 * On créé un champ par propriété.
	 * @param doc le document à remplir
	 * @param o la bean d'où extraire les propriétés
	 * @param prefix un préfixe à ajouter devant les noms de champs
	 * @param fmt un formateur pour transformer les dates en texte (si null, on créé des champs NotesDateTime)
	 * @throws NotesException en cas de problème
	 */
	public final static void fillDocument(Document doc, Object o, String prefix, DateFormat fmt) throws NotesException {
		fillDocument(doc, o, prefix, fmt, null);
	}
	
	/**
	 * Rempli un document à partir d'une bean.
	 * On créé un champ par propriété.
	 * @param doc le document à remplir
	 * @param o la bean d'où extraire les propriétés
	 * @param prefix un préfixe à ajouter devant les noms de champs
	 * @param fmt un formateur pour transformer les dates en texte (si null, on créé des champs NotesDateTime)
	 * @param rtFields champs rich text à générer
	 * @throws NotesException en cas de problème
	 */
	@SuppressWarnings("unchecked")
	public final static void fillDocument(Document doc, Object o, String prefix, DateFormat fmt, String[] rtFields) throws NotesException {
		try {
			Set<String> sRtFields = new HashSet<String>();
			if( rtFields != null ) {
				for( int i=0; i<rtFields.length; i++ )
					sRtFields.add(rtFields[i]);
			}
			
			// La session courante
			Session session = doc.getParentDatabase().getParent();
			
			// On l'introspecte pour ne pas mettre les champs en dur
			Class<?> cl = o.getClass();
			BeanInfo beanInfo = Introspector.getBeanInfo(cl);
			PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
			
			// Parcours les propriétés de l'objet
			for( int i=0; i<descriptors.length; i++ ) {
				PropertyDescriptor descriptor = descriptors[i];
				String name = descriptor.getName();
				
				// On ne tient pas compte de "class"
				if( "class".equals(name) )
					continue;
				
				// Récupère le getter et son type de retour
				Method getter = descriptor.getReadMethod();
				Class<?> returnType = getter.getReturnType();
				
				// Récupère la future valeure du champ. On récupère quoi qu'il se passe une valeur multi 
				// (quitte à n'avoir qu'un élément dans ce vecteur)
				Object v = getter.invoke(o, new Object[] {});
				
				// Gestion du cas null => On supprime le champ
				if( v == null ) {
					doc.removeItem(prefix == null ? name : prefix + name);
					continue;
				}
				
				// Pas null, on transforme la valeur
				List<Object> values;
				if( returnType.isAssignableFrom(List.class) )
					values = (List<Object>) v;
				else {
					values = new ArrayList<Object>();
					values.add(v);
				}
				
				// Converti les dates, et on vérifie qu'on n'a que des types supportés
				// Domino attend un vecteur
				Vector<Object> convertedValues = new Vector<Object>();
				for( Object value : values ) {
					Object convertedValue;
					Class<?> valueClass = value.getClass();
					
					// Si on tombe sur un type qu'on ne supporte pas, on fait un toString
					if( !isSupported(valueClass) ) {
						convertedValue = value.toString();
					
					// Si c'est une date, et qu'on a un DateFormat, on la converti en chaîne
					} else if( returnType.isAssignableFrom(Date.class) && fmt != null ) {
						convertedValue = fmt.format(value);
					
					// Si c'est une date, mais qu'on n'a pas de DateFormat, on en fait un NotesDateTime
					} else if( returnType.isAssignableFrom(Date.class) ) {
						convertedValue = session.createDateTime((Date) value);
					
					// Si c'est un boolean, on le converti en chaîne
					} else if( valueClass.isAssignableFrom(Boolean.class) ) {
						boolean b = ((Boolean) v).booleanValue();
						convertedValue = b ? "1" : "0";
						
					// Si c'est une liste, on le converti les dates
					} else if( valueClass.isAssignableFrom(List.class) ) {
						Vector<Object> vec = new Vector<Object>();
						List<Object> lst = (List<Object>) value;
						for( Object obj : lst ) {
							if( obj.getClass().isAssignableFrom(Date.class) )
								vec.add(session.createDateTime((Date) obj));
							else
								vec.add(obj);
						}
						vec.addAll((List<Object>) value);
						convertedValue = vec;
						
					// Sinon, on prend la valeur telle quelle
					} else {
						convertedValue = value;
					}
					
					convertedValues.add(convertedValue);
				}
				
				// Stock la valeur dans le champ.
				boolean inRichText = false;
				if( convertedValues.size() == 1 )
					if( convertedValues.get(0).getClass().equals(String.class) )
						if( sRtFields.contains(name) )
							inRichText = true;
				
				if( inRichText ) {
					doc.removeItem(prefix == null ? name : prefix + name);
					RichTextItem rtit = doc.createRichTextItem(prefix == null ? name : prefix + name);
					rtit.appendText((String) convertedValues.get(0));
				} else
					doc.replaceItemValue(prefix == null ? name : prefix + name, convertedValues);
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
