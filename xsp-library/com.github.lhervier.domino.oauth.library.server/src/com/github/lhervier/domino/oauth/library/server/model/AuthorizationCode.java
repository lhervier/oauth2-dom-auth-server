package com.github.lhervier.domino.oauth.library.server.model;

import java.util.List;

/**
 * Un code autorisation.
 * @author Lionel HERVIER
 */
public class AuthorizationCode extends AccessToken {

	/**
	 * Le champ lecteur qui contient le nom de l'application
	 */
	private String application;
	
	/**
	 * L'identifiant
	 */
	private String id;
	
	/**
	 * L'uri de redirection
	 */
	private String redirectUri;
	
	/**
	 * L'id client de l'application
	 */
	private String clientId;
	
	/**
	 * La date d'expiration
	 */
	private long expires;
	
	/**
	 * L'utilisateur pour lequel a été généré le code autorisation
	 */
	private String user;
	
	/**
	 * Le scopes demandé
	 */
	private List<String> scopes;
	
	/**
	 * Le scopes autorisé
	 */
	private List<String> grantedScopes;
	
	/**
	 * @return the application
	 */
	public String getApplication() {
		return application;
	}

	/**
	 * @param application the application to set
	 */
	public void setApplication(String application) {
		this.application = application;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
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
	 * @return the expires
	 */
	public long getExpires() {
		return expires;
	}

	/**
	 * @param expires the expires to set
	 */
	public void setExpires(long expires) {
		this.expires = expires;
	}

	/**
	 * @return the scopes
	 */
	public List<String> getScopes() {
		return scopes;
	}

	/**
	 * @param scopes the scopes to set
	 */
	public void setScopes(List<String> scope) {
		this.scopes = scope;
	}

	/**
	 * @return the grantedScopes
	 */
	public List<String> getGrantedScopes() {
		return grantedScopes;
	}

	/**
	 * @param grantedScopes the grantedScopes to set
	 */
	public void setGrantedScopes(List<String> grantedScope) {
		this.grantedScopes = grantedScope;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
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
}
