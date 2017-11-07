package com.github.lhervier.domino.oauth.server.ext;

import java.util.List;

import lotus.domino.NotesException;

import com.github.lhervier.domino.oauth.server.NotesUserPrincipal;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;

/**
 * Interface à implémenter quand on ajoute un scope
 * @author Lionel HERVIER
 */
public interface IOAuthExtension<T> {

	/**
	 * Retourne un id
	 */
	public String getId();
	
	/**
	 * Return the context class
	 */
	public Class<T> getContextClass();
	
	/**
	 * Check if this extension will process the request
	 * @return true if the extension will process the request
	 */
	public boolean validateResponseTypes(List<String> responseTypes);
	
	/**
	 * Initialization of the context.
	 * @param session the notes session (opened as the user)
	 * @param granter pour déclarer d'un scope a été autorisé.
	 * @param clientId l'id client de l'application demandée.
	 * @param scopes les scopes demandés
	 * @return le contexte ou null pour ne pas participer.
	 * @throws NotesException en ca de pb
	 */
	public T initContext(
			NotesUserPrincipal user,
			IScopeGranter granter, 
			String clientId, 
			List<String> scopes) throws NotesException;
	
	/**
	 * Process authorization code
	 * @param responseTypes the response types
	 * @param authCode the authorization code
	 * @param adder the property adder to add property in the query string
	 */
	public void authorize(
			T context,
			List<String> responseTypes, 
			AuthCodeEntity authCode,
			IPropertyAdder adder) throws NotesException;
	
	/**
	 * Génère des attributs à ajouter à la réponse de grant.
	 * Cette méthode est appelée lors de la génération du token.
	 * Ce code est appelé alors que la session courante est ouverte en tant que l'application.
	 * @param context le contexte généré lors de l'appel à authorize
	 * @param adder pour ajouter des propriétés à la réponse au grant.
	 * @param scopes les scopes demandés
	 * @throws NotesException en ca de pb
	 */
	public void token(
			T context, 
			IPropertyAdder adder,
			List<String> scopes) throws NotesException;
}
