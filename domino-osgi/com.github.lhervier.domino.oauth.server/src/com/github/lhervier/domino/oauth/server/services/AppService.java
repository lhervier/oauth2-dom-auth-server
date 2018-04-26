package com.github.lhervier.domino.oauth.server.services;

import java.util.List;

import com.github.lhervier.domino.oauth.server.model.Application;

/**
 * Service to manipualte applications
 * @author Lionel HERVIER
 */
public interface AppService {
	
	/**
	 * Retourne les noms des applications
	 * @return les noms des applications
	 */
	public List<String> getApplicationsNames();
	
	/**
	 * Retourne une application depuis son nom
	 * @param appName le nom de l'application
	 * @return l'application
	 */
	public Application getApplicationFromName(String appName);
	
	/**
	 * Retourne une application depuis son client_id
	 * @param clientId l'id du client
	 * @return l'application
	 */
	public Application getApplicationFromClientId(String clientId);
	
	/**
	 * Prépare une future nouvelle app
	 * @return une nouvelle app (avec un client_id seulement)
	 */
	public Application prepareApplication();
	
	/**
	 * Ajoute une application
	 * @param app l'application à ajouter
	 * @return le secret
	 */
	public String addApplication(Application app);
	
	/**
	 * Met à jour une application
	 * @param app l'application à mettre à jour
	 */
	public void updateApplication(Application app);
	
	/**
	 * Supprime une application
	 * @param name le nom de l'application
	 */
	public void removeApplication(String name);
	
	/**
	 * Remove an application
	 * @param clientId the app clientId
	 */
	public void removeApplicationFromClientId(String clientId);
}
