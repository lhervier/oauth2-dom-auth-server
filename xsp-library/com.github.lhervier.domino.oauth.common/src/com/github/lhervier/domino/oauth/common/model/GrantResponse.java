package com.github.lhervier.domino.oauth.common.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Réponse pour les types de grant "authorization code"
 * @author Lionel HERVIER
 */
public class GrantResponse {

	/**
	 * Le token d'acces
	 */
	@SerializedName("access_token")
	private String accessToken;
	
	/**
	 * La durée de validité
	 */
	@SerializedName("expires_in")
	private long expiresIn;
	
	/**
	 * Le refresh token
	 */
	@SerializedName("refresh_token")
	private String refreshToken;
	
	/**
	 * Le type de token
	 */
	@SerializedName("token_type")
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
}
