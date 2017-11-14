package com.github.lhervier.domino.oauth.server.repo;

import java.util.List;

import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;

/**
 * Repository to access applications
 * @author Lionel HERVIER
 */
public interface ApplicationRepository {
	
	/**
	 * Return the applications names.
	 * We are using the user rights to access the oauth2 database.
	 * @return the registered application names
	 */
	public List<String> listNames();
	
	/**
	 * Retourne une application depuis son nom
	 * @param appName le nom de l'application
	 * @return l'application
	 */
	public ApplicationEntity findOneByName(String appName);
	
	/**
	 * Retourne une application depuis son client_id
	 * @param clientId l'id du client
	 * @return l'application
	 */
	public ApplicationEntity findOne(String clientId);
	
	/**
	 * Add a new application
	 * @param app the application to create
	 */
	public ApplicationEntity save(ApplicationEntity app);
	
	/**
	 * Supprime une application
	 * @param name le nom de l'application
	 */
	public void deleteByName(String name);
}
