package com.github.lhervier.domino.oauth.server.repo;

import java.util.List;

import lotus.domino.NotesException;

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
	 * @throws NotesException en cas de pb
	 */
	public List<String> listNames() throws NotesException;
	
	/**
	 * Retourne une application depuis son nom
	 * @param appName le nom de l'application
	 * @return l'application
	 * @throws NotesException en cas de pb
	 */
	public ApplicationEntity findOneByName(String appName) throws NotesException;
	
	/**
	 * Retourne une application depuis son client_id
	 * @param clientId l'id du client
	 * @return l'application
	 * @throws NotesException en cas de pb
	 */
	public ApplicationEntity findOne(String clientId) throws NotesException;
	
	/**
	 * Add a new application
	 * @param app the application to create
	 * @throws NotesException en cas de pb
	 */
	public ApplicationEntity save(ApplicationEntity app) throws NotesException;
	
	/**
	 * Supprime une application
	 * @param name le nom de l'application
	 * @throws NotesException en cas de pb
	 */
	public void deleteByName(String name) throws NotesException;
}
