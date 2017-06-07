package com.github.lhervier.domino.oauth.library.server.ext;

import java.util.List;

import lotus.domino.NotesException;

import com.google.gson.JsonObject;

/**
 * Interface à implémenter quand on ajoute un scope
 * @author Lionel HERVIER
 */
public interface IOAuthExtension {

	/**
	 * Retourne un id
	 */
	public String getId();
	
	/**
	 * Appelé lors de l'utilisation du endpoint authorize.
	 * Les informations retournées seront ajoutées au code autorisation.
	 * Ce code est appelé alors que la session courante est ouverte en tant que l'utilisateur qui cherche à se connecter.
	 * @param granter pour déclarer d'un scope a été autorisé.
	 * @param clientId l'id client de l'application demandée.
	 * @param scopes les scopes demandés
	 * @return le contexte ou null pour ne pas participer.
	 * @throws NotesException en ca de pb
	 */
	public JsonObject authorize(IScopeGranter granter, String clientId, List<String> scopes) throws NotesException;
	
	/**
	 * Génère des attributs à ajouter à la réponse de grant.
	 * Cette méthode est appelée lors de la génération du token.
	 * Ce code est appelé alors que la session courante est ouverte en tant que l'application.
	 * @param context le contexte généré lors de l'appel à authorize
	 * @param adder pour ajouter des propriétés à la réponse au grant.
	 * @param scopes les scopes demandés
	 * @throws NotesException en ca de pb
	 */
	public void token(JsonObject context, IPropertyAdder adder) throws NotesException;
	
	/**
	 * Génère les attributs à ajouter à la réponse de grant.
	 * Cette méthode est appelée lors du rafraîchissement d'un token.
	 * Ce code est appelé alors que la session courante est ouverte en tant que l'application.
	 * @param context le contexte généré lors de l'appel à authorize.
	 * @param adder pour ajouter des propriétés à la réponse au grant
	 * @param granter pour déclarer les scopes accordés
	 * @param scopes les scopes demandés
	 * @throws NotesException en ca de pb
	 */
	public void refresh(JsonObject context, IPropertyAdder adder, IScopeGranter granter, List<String> scopes) throws NotesException;
	
}
