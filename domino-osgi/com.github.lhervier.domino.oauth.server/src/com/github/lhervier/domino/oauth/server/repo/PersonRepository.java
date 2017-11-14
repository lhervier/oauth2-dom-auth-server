package com.github.lhervier.domino.oauth.server.repo;

import com.github.lhervier.domino.oauth.server.entity.PersonEntity;

public interface PersonRepository {
	/**
	 * Save a person (user the current user rights)
	 */
	public PersonEntity save(PersonEntity entity);
	
	/**
	 * Return the person with the given name.
	 * The search is made using server rights.
	 */
	public PersonEntity findOne(String fullName);
	
	/**
	 * Remove a person
	 */
	public void delete(String fullName);
}
