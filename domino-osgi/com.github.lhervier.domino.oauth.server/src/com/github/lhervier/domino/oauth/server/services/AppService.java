package com.github.lhervier.domino.oauth.server.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import lotus.domino.NotesException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.BaseServerComponent;
import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.entity.PersonEntity;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.repo.ApplicationRepository;
import com.github.lhervier.domino.oauth.server.repo.PersonRepository;

/**
 * Service to manipualte applications
 * @author Lionel HERVIER
 */
@Service
public class AppService extends BaseServerComponent {
	
	/**
	 * The application root
	 */
	@Value("${oauth2.server.applicationRoot}")
	private String applicationRoot;
	
	/**
	 * The application repository
	 */
	@Autowired
	private ApplicationRepository appRepo;
	
	/**
	 * The Person repository
	 */
	@Autowired
	private PersonRepository personRepo;
	
	/**
	 * Convert an entity to an application
	 * @param entity the entity
	 * @return the application
	 */
	private Application fromEntity(ApplicationEntity entity) {
		if( entity == null )
			return null;
		
		Application app = new Application();
		app.setAppReader(entity.getAppReader());
		app.setClientId(entity.getClientId());
		app.setName(entity.getName());
		app.setReaders(entity.getReaders());
		app.setRedirectUri(entity.getRedirectUri());
		app.setRedirectUris(new ArrayList<String>());
		app.getRedirectUris().addAll(entity.getRedirectUris());
		
		app.setFullName("CN=" + entity.getName() + this.applicationRoot);
		
		return app;
	}
	
	/**
	 * Convert an application to an entity
	 * @param app the application
	 * @return the entity
	 */
	private ApplicationEntity toEntity(Application app) {
		if( app == null )
			return null;
		
		ApplicationEntity entity = new ApplicationEntity();
		entity.setAppReader(app.getAppReader());
		entity.setClientId(app.getClientId());
		entity.setName(app.getName());
		entity.setReaders(app.getReaders());
		entity.setRedirectUri(app.getRedirectUri());
		entity.setRedirectUris(new ArrayList<String>());
		entity.getRedirectUris().addAll(app.getRedirectUris());
		
		return entity;
	}
	
	/**
	 * Retourne les noms des applications
	 * @return les noms des applications
	 * @throws NotesException en cas de pb
	 */
	public List<String> getApplicationsNames() throws NotesException {
		return this.appRepo.listNames();
	}
	
	/**
	 * Retourne une application depuis son nom
	 * @param appName le nom de l'application
	 * @return l'application
	 * @throws NotesException en cas de pb
	 */
	public Application getApplicationFromName(String appName) throws NotesException {
		return this.fromEntity(this.appRepo.findOneByName(appName));
	}
	
	/**
	 * Retourne une application depuis son client_id
	 * @param clientId l'id du client
	 * @return l'application
	 * @throws NotesException en cas de pb
	 */
	public Application getApplicationFromClientId(String clientId) throws NotesException {
		return this.fromEntity(this.appRepo.findOne(clientId));
	}
	
	/**
	 * Prépare une future nouvelle app
	 * @return une nouvelle app (avec un client_id seulement)
	 */
	public Application prepareApplication() {
		Application ret = new Application();
		ret.setName("");
		ret.setClientId(UUID.randomUUID().toString());
		ret.setRedirectUri("");
		ret.setRedirectUris(new ArrayList<String>());
		ret.setReaders("*");
		return ret;
	}
	
	/**
	 * Ajoute une application
	 * @param app l'application à ajouter
	 * @return le secret
	 * @throws NotesException en cas de pb
	 */
	public String addApplication(Application app) throws NotesException {
		// Check that it does not already exist
		ApplicationEntity existing = this.appRepo.findOneByName(app.getName());
		if( existing != null )
			throw new NotesException(-1, "Application '" + app.getName() + "' already exists");
		existing = this.appRepo.findOne(app.getClientId());
		if( existing != null )
			throw new NotesException(-1, "Application with client id '" + app.getClientId() + "' already exists");
		
		// Compute the full name
		app.setFullName("CN=" + app.getName() + this.applicationRoot);
		
		// Create the associated person
		PersonEntity person = new PersonEntity();
		person.setFullNames(Arrays.asList(app.getFullName(), app.getClientId()));
		person.setLastName(app.getName());
		person.setShortName(app.getName());
		person = this.personRepo.save(person);
		String pwd = person.getHttpPassword();
		
		// Save the application
		this.appRepo.save(this.toEntity(app));
		
		return pwd;
	}
	
	/**
	 * Met à jour une application
	 * @param app l'application à mettre à jour
	 * @throws NotesException en cas de pb
	 */
	public void updateApplication(Application app) throws NotesException {
		ApplicationEntity existing = this.appRepo.findOneByName(app.getName());
		if( existing == null )
			throw new NotesException(-1, "Application '" + app.getName() + "' does not exist...");
		
		this.appRepo.save(this.toEntity(app));
	}
	
	/**
	 * Supprime une application
	 * @param name le nom de l'application
	 * @throws NotesException en cas de pb
	 */
	public void removeApplication(String name) throws NotesException {
		Application app = this.getApplicationFromName(name);
		if( app == null )
			return;
		
		this.personRepo.delete(app.getFullName());
		this.appRepo.deleteByName(name);
	}
}
