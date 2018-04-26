package com.github.lhervier.domino.oauth.server.services.impl;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.entity.ApplicationEntity;
import com.github.lhervier.domino.oauth.server.entity.PersonEntity;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.model.ClientType;
import com.github.lhervier.domino.oauth.server.repo.ApplicationRepository;
import com.github.lhervier.domino.oauth.server.repo.PersonRepository;
import com.github.lhervier.domino.oauth.server.services.AppService;
import com.github.lhervier.domino.oauth.server.utils.Utils;

/**
 * Service to manipulate applications
 * @author Lionel HERVIER
 */
@Service
public class AppServiceImpl implements AppService {
	
	/**
	 * Client types
	 */
	public static final String CLIENTTYPE_CONFIDENTIAL = ClientType.CONFIDENTIAL.name();
	public static final String CLIENTTYPE_PUBLIC = ClientType.PUBLIC.name();
	
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
		app.setClientId(entity.getClientId());
		app.setName(entity.getName());
		if( entity.getClientType() != null )
			app.setClientType(ClientType.valueOf(entity.getClientType()));
		else
			app.setClientType(ClientType.PUBLIC);
		app.setReaders(entity.getReaders());
		app.setRedirectUri(entity.getRedirectUri());
		app.getRedirectUris().addAll(entity.getRedirectUris());
		app.setFullName(entity.getFullName());
		
		return app;
	}
	
	/**
	 * Convert an application to an entity
	 * @param app the application
	 * @return the entity
	 */
	private ApplicationEntity toEntity(Application app) {
		ApplicationEntity entity = new ApplicationEntity();
		entity.setAppReader(app.getFullName());
		entity.setClientId(app.getClientId());
		entity.setName(app.getName());
		entity.setFullName("CN=" + app.getName() + this.applicationRoot);
		entity.setReaders(app.getReaders());
		if( app.getClientType() != null )
			entity.setClientType(app.getClientType().name());
		else
			entity.setClientType(ClientType.PUBLIC.name());
		String error = Utils.checkRedirectUri(app.getRedirectUri());
		if( error != null )
			throw new DataIntegrityViolationException(error);
		entity.setRedirectUri(app.getRedirectUri());

		for( String uri : app.getRedirectUris() ) {
			error = Utils.checkRedirectUri(uri);
			if( error != null )
				throw new DataIntegrityViolationException(error);
			entity.getRedirectUris().add(uri);
		}
		
		return entity;
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.AppService#getApplicationsNames()
	 */
	public List<String> getApplicationsNames() {
		return this.appRepo.listNames();
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.AppService#getApplicationFromName(java.lang.String)
	 */
	public Application getApplicationFromName(String appName) {
		return this.fromEntity(this.appRepo.findOneByName(appName));
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.AppService#getApplicationFromClientId(java.lang.String)
	 */
	public Application getApplicationFromClientId(String clientId) {
		return this.fromEntity(this.appRepo.findOne(clientId));
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.AppService#prepareApplication()
	 */
	public Application prepareApplication() {
		Application ret = new Application();
		ret.setName("");
		ret.setClientId(UUID.randomUUID().toString());
		ret.setRedirectUri("");
		ret.setReaders("*");
		ret.setClientType(ClientType.PUBLIC);
		return ret;
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.AppService#addApplication(com.github.lhervier.domino.oauth.server.model.Application)
	 */
	public String addApplication(Application app) {
		// Check that it does not already exist
		ApplicationEntity existing = this.appRepo.findOneByName(app.getName());
		if( existing != null )
			throw new DataIntegrityViolationException("Application '" + app.getName() + "' already exists");
		existing = this.appRepo.findOne(app.getClientId());
		if( existing != null )
			throw new DataIntegrityViolationException("Application with client id '" + app.getClientId() + "' already exists");
		
		// Compute the full name
		app.setFullName("CN=" + app.getName() + this.applicationRoot);
		
		// Create the associated person
		PersonEntity person = new PersonEntity();
		person.setFullNames(Arrays.asList(app.getFullName(), app.getClientId()));
		person.setLastName(app.getName());
		person.setShortName(app.getName());
		
		// Save the application and the user
		this.appRepo.save(this.toEntity(app));
		person = this.personRepo.save(person);
		
		return person.getHttpPassword();
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.AppService#updateApplication(com.github.lhervier.domino.oauth.server.model.Application)
	 */
	public void updateApplication(Application app) {
		ApplicationEntity existing = this.appRepo.findOne(app.getClientId());
		if( existing == null )
			throw new DataIntegrityViolationException("Application does not exist...");
		if( !Utils.equals(app.getName(), existing.getName()) )
			throw new DataIntegrityViolationException("Cannot change name of application...");
		app.setFullName(existing.getFullName());	// Not changing full name
		
		this.appRepo.save(this.toEntity(app));
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.AppService#removeApplication(java.lang.String)
	 */
	public void removeApplication(String name) {
		Application app = this.getApplicationFromName(name);
		if( app == null )
			throw new DataIntegrityViolationException("Application does not exist...");
		
		this.personRepo.delete(app.getFullName());
		this.appRepo.deleteByName(name);
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.services.AppService#removeApplicationFromClientId(java.lang.String)
	 */
	@Override
	public void removeApplicationFromClientId(String clientId) {
		Application app = this.getApplicationFromClientId(clientId);
		if( app == null )
			throw new DataIntegrityViolationException("Application does not exist...");
		
		this.personRepo.delete(app.getFullName());
		this.appRepo.deleteByName(app.getName());
	}
}
