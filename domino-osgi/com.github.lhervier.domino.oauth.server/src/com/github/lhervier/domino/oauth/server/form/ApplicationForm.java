package com.github.lhervier.domino.oauth.server.form;

import java.util.List;

public class ApplicationForm {

	/**
	 * The action to execute
	 */
	private String action;
	
	/**
	 * The existing redirect uris
	 */
	private List<String> existingRedirectUris;
	
	/**
	 * The application clientId
	 */
	private String clientId;
	
	/**
	 * The application client type
	 */
	private String clientType;
	
	/**
	 * The application secret
	 */
	private String secret;
	
	/**
	 * The application name
	 */
	private String name;
	private String nameError = null;
	
	/**
	 * The readers
	 */
	private String readers;
	private String readersError = null;
	
	/**
	 * The redirect uri
	 */
	private String redirectUri;
	private String redirectUriError = null;
	
	/**
	 * A redirect uri to add
	 */
	private String newRedirectUri;
	private String newRedirectUriError = null;
	
	public boolean isError() {
		return (nameError != null) || (readersError != null) || (redirectUriError != null) || (newRedirectUriError != null);
	}

	/**
	 * @return the existingRedirectUris
	 */
	public List<String> getExistingRedirectUris() {
		return existingRedirectUris;
	}

	/**
	 * @param existingRedirectUris the existingRedirectUris to set
	 */
	public void setExistingRedirectUris(List<String> existingRedirectUris) {
		this.existingRedirectUris = existingRedirectUris;
	}

	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the secret
	 */
	public String getSecret() {
		return secret;
	}

	/**
	 * @param secret the secret to set
	 */
	public void setSecret(String secret) {
		this.secret = secret;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the nameError
	 */
	public String getNameError() {
		return nameError;
	}

	/**
	 * @param nameError the nameError to set
	 */
	public void setNameError(String nameError) {
		this.nameError = nameError;
	}

	/**
	 * @return the readers
	 */
	public String getReaders() {
		return readers;
	}

	/**
	 * @param readers the readers to set
	 */
	public void setReaders(String readers) {
		this.readers = readers;
	}

	/**
	 * @return the readersError
	 */
	public String getReadersError() {
		return readersError;
	}

	/**
	 * @param readersError the readersError to set
	 */
	public void setReadersError(String readersError) {
		this.readersError = readersError;
	}

	/**
	 * @return the redirectUri
	 */
	public String getRedirectUri() {
		return redirectUri;
	}

	/**
	 * @param redirectUri the redirectUri to set
	 */
	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	/**
	 * @return the redirectUriError
	 */
	public String getRedirectUriError() {
		return redirectUriError;
	}

	/**
	 * @param redirectUriError the redirectUriError to set
	 */
	public void setRedirectUriError(String redirectUriError) {
		this.redirectUriError = redirectUriError;
	}

	/**
	 * @return the newRedirectUri
	 */
	public String getNewRedirectUri() {
		return newRedirectUri;
	}

	/**
	 * @param newRedirectUri the newRedirectUri to set
	 */
	public void setNewRedirectUri(String newRedirectUri) {
		this.newRedirectUri = newRedirectUri;
	}

	/**
	 * @return the newRedirectUriError
	 */
	public String getNewRedirectUriError() {
		return newRedirectUriError;
	}

	/**
	 * @param newRedirectUriError the newRedirectUriError to set
	 */
	public void setNewRedirectUriError(String newRedirectUriError) {
		this.newRedirectUriError = newRedirectUriError;
	}

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @return the clientType
	 */
	public String getClientType() {
		return clientType;
	}

	/**
	 * @param clientType the clientType to set
	 */
	public void setClientType(String clientType) {
		this.clientType = clientType;
	}
	
}
