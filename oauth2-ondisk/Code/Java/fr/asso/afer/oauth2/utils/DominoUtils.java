package fr.asso.afer.oauth2.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
	 * Pour la convertion des types primitifs en types Objets
	 */
	private final static Map<Class<?>, Class<?>> primitives = new HashMap<Class<?>, Class<?>>();
	static {
		primitives.put(boolean.class, Boolean.class);
		primitives.put(byte.class, Byte.class);
		primitives.put(short.class, Short.class);
		primitives.put(char.class, Character.class);
		primitives.put(int.class, Integer.class);
		primitives.put(long.class, Long.class);
		primitives.put(float.class, Float.class);
		primitives.put(double.class, Double.class);
	}
	
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
		supported.add(Date.class);
		supported.add(boolean.class);
		supported.add(Boolean.class);
		supported.add(Vector.class);
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
	 * Ouvre une database
	 * @param session la session pour ouvrir la base
	 * @param filePath le chemin vers la base
	 * @return la database ou null si elle n'existe pas
	 * @throws NotesException si on n'a pas les droits
	 */
	public static final Database openDatabase(Session session, String name) throws NotesException {
		Database db = session.getDatabase(null, name, false);		// createOnFail = false
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
	 * Pour forcer un rafraîchissement du NAB
	 * @param nab la nab à rafraîchir
	 * @throws NotesException en cas de pb
	 */
	public static final void refreshNab(Database nab) throws NotesException {
		Session session = nab.getParent();
		session.sendConsoleCommand(session.getServerName(), "load updall -R " + nab.getFilePath());
		// FIXME: Attendre que le updall soit terminé !!
		session.sendConsoleCommand(session.getServerName(), "dbcache flush");
		session.sendConsoleCommand(session.getServerName(), "show nlcache reset");
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
	@SuppressWarnings("unchecked")
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
					paramClass = primitives.get(parameters[0]);
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
				else if( paramClass.isAssignableFrom(Vector.class) ) {
					Item it = doc.getFirstItem(name);
					Vector<Object> values;
					if( it.getType() == Item.DATETIMES ) {
						Vector<Object> dts = doc.getItemValueDateTimeArray(name);
						values = new Vector<Object>();
						for( Iterator<?> iterator = dts.iterator(); iterator.hasNext(); ) {
							DateTime dt = (DateTime) iterator.next();
							values.add(dt.toJavaDate());
						}
					} else {
						values = doc.getItemValue(name);
					}
					setter.invoke(o, new Object[] {values});
					
				// DateTime: On converti en date java
				} else if( paramClass.isAssignableFrom(Date.class) ) {
					Item it = doc.getFirstItem(name);
					Date dt = null;
					
					// Un vrai champ date
					if( it.getType() == Item.DATETIMES ) {
						Vector<DateTime> values = doc.getItemValueDateTimeArray(name);
						if( values != null && values.size() != 0 )
							dt = values.get(0).toJavaDate();
						
					// Un champ texte qu'on va interprêter en date
					} else if( it.getType() == Item.TEXT ) {
						String s = doc.getItemValueString(name);
						try {
							dt = fmt.parse(s);
						} catch(ParseException e) {
							dt = null;
						}
					}
					setter.invoke(o, new Object[] {dt});
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
		}
	}
}
