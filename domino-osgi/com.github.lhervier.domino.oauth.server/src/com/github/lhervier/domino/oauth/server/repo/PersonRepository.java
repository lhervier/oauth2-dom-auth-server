package com.github.lhervier.domino.oauth.server.repo;

import lotus.domino.NotesException;

import com.github.lhervier.domino.oauth.server.entity.PersonEntity;

public interface PersonRepository {
	/**
	 * Save a person (user the current user rights)
	 */
	public PersonEntity save(PersonEntity entity);
	
	/**
	 * Return the person with the given name.
	 * The search is made using server rights.
	 * @throws NotesException
	 */
	public PersonEntity findOne(String fullName) throws NotesException;
	
	/**
	 * Remove a person
	 */
	public void delete(String fullName) throws NotesException;
}
