package com.github.lhervier.domino.oauth.server.ext;

import java.util.List;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;

/**
 * Interface à implémenter quand on ajoute un scope
 * @author Lionel HERVIER
 */
public interface IOAuthExtension<T> {

	/**
	 * Return the context class
	 */
	public Class<T> getContextClass();
	
	/**
	 * Initialization of the context.
	 * @param session the notes session (opened as the user)
	 * @param granter pour déclarer d'un scope a été autorisé.
	 * @param clientId l'id client de l'application demandée.
	 * @param scopes les scopes demandés
	 * @return le contexte ou null pour ne pas participer.
	 */
	public T initContext(
			NotesPrincipal user,
			IScopeGranter granter, 
			String clientId, 
			List<String> scopes);
	
	/**
	 * Process authorization code
	 * @param responseTypes the response types
	 * @param authCode the authorization code
	 * @param adder the property adder to add property in the query string
	 */
	public void authorize(
			T context,
			AuthCodeEntity authCode,
			IPropertyAdder adder);
	
	/**
	 * Génère des attributs à ajouter à la réponse de grant.
	 * Cette méthode est appelée lors de la génération du token.
	 * Ce code est appelé alors que la session courante est ouverte en tant que l'application.
	 * @param context le contexte généré lors de l'appel à authorize
	 * @param adder pour ajouter des propriétés à la réponse au grant.
	 * @param authCode the authorization code
	 */
	public void token(
			T context, 
			IPropertyAdder adder,
			AuthCodeEntity authCode);
}
