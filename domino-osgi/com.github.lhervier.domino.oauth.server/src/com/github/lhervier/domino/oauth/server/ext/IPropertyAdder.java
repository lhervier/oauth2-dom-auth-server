package com.github.lhervier.domino.oauth.server.ext;

/**
 * Pour ajouter une propriété à la réponse du grant
 * @author Lionel HERVIER
 */
public interface IPropertyAdder {

	/**
	 * Ajoute une propriété signée
	 * @param name le nom de la propriété
	 * @param obj l'objet à serialiser
	 * @param kid the key id to sign
	 */
	public void addSignedProperty(String name, Object obj, String kid);
	
	/**
	 * Add a standard property
	 * @param name the property name
	 * @param obj the object to add
	 */
	public void addProperty(String name, Object value);
}
