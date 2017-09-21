package com.github.lhervier.domino.oauth.library.server.ext;


/**
 * Pour ajouter une propriété à la réponse du grant
 * @author Lionel HERVIER
 */
public interface IPropertyAdder {

	/**
	 * Ajoute une propriété signée
	 * @param name le nom de la propriété
	 * @param obj l'objet à serialiser
	 */
	public void addSignedProperty(String name, Object obj);
	
	/**
	 * Ajoute une propriété cryptée
	 * @param name le nom de la propriété
	 * @param obj l'objet à serialiser
	 */
	public void addCryptedProperty(String name, Object obj);
}
