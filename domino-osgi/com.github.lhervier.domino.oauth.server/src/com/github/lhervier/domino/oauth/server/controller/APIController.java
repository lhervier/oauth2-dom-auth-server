package com.github.lhervier.domino.oauth.server.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.github.lhervier.domino.oauth.server.ex.APIException;
import com.github.lhervier.domino.oauth.server.ex.ForbiddenException;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ex.WrongPathException;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.model.BaseApplication;

/**
 * Administration API controller
 * @author Lionel HERVIER
 */
public interface APIController {

	/**
	 * List all the applications
	 * @return the applications list
	 */
	List<BaseApplication> listApplications() throws NotAuthorizedException, ForbiddenException, WrongPathException, APIException;
	
	/**
	 * Return the detail of an application
	 * @param clientId the app client id
	 * @return the app details
	 */
	ResponseEntity<Application> getApplication(String clientId) throws NotAuthorizedException, ForbiddenException, WrongPathException, APIException;
	
	/**
	 * Response from the newapplication endpoint
	 */
	public static class NewApplicationResponse {
		private String clientId;
		private String secret;
		public String getClientId() { return clientId; }
		public void setClientId(String clientId) { this.clientId = clientId; }
		public String getSecret() { return secret; }
		public void setSecret(String secret) { this.secret = secret; }
	}
	
	/**
	 * Creates a new application
	 * @param app the application to create
	 * @return the new application generated secret
	 */
	NewApplicationResponse newApplication(Application app) throws NotAuthorizedException, ForbiddenException, WrongPathException, APIException;
	
	/**
	 * Updates an application
	 * @param app the application to update
	 */
	Map<String, String> updateApplication(String clientId, Application app) throws NotAuthorizedException, ForbiddenException, WrongPathException, APIException;
	
	/**
	 * Remove an application
	 * @param clientId the app client id
	 */
	Map<String, String> removeApplication(String clientId) throws NotAuthorizedException, ForbiddenException, WrongPathException, APIException;
}
