package com.github.lhervier.domino.oauth.client.model;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Réponse pour les types de grant "authorization code"
 * @author Lionel HERVIER
 */
public class GrantResponse {

	/**
	 * Le token d'acces
	 */
	@JsonProperty("access_token")
	private String accessToken;
	
	/**
	 * La durée de validité
	 */
	@JsonProperty("expires_in")
	private long expiresIn;
	
	/**
	 * Le refresh token
	 */
	@JsonProperty("refresh_token")
	private String refreshToken;
	
	/**
	 * Le id_token openid
	 */
	@JsonProperty("id_token")
	private String idToken;
	
	/**
	 * Le type de token
	 */
	@JsonProperty("token_type")
	private String tokenType;

	/**
	 * Les scopes (s'il sont différents de ceux demandés par le client)
	 */
	private List<String> scopes;
	
	/**
	 * @return the accessToken
	 */
	public String getAccessToken() {
		return accessToken;
	}

	/**
	 * @param accessToken the accessToken to set
	 */
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	/**
	 * @return the expiresIn
	 */
	public long getExpiresIn() {
		return expiresIn;
	}

	/**
	 * @param expiresIn the expiresIn to set
	 */
	public void setExpiresIn(long expiresIn) {
		this.expiresIn = expiresIn;
	}

	/**
	 * @return the refreshToken
	 */
	public String getRefreshToken() {
		return refreshToken;
	}

	/**
	 * @param refreshToken the refreshToken to set
	 */
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	/**
	 * @return the tokenType
	 */
	public String getTokenType() {
		return tokenType;
	}

	/**
	 * @param tokenType the tokenType to set
	 */
	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
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
	 * @return the idToken
	 */
	public String getIdToken() {
		return idToken;
	}

	/**
	 * @param idToken the idToken to set
	 */
	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}
}
