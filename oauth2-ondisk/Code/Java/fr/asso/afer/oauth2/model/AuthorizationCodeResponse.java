package fr.asso.afer.oauth2.model;

import fr.asso.afer.oauth2.utils.JsonUtils.SerializedName;

/**
 * Réponse pour les types de grant "authorization code"
 * @author Lionel HERVIER
 */
public class AuthorizationCodeResponse {

	/**
	 * Le token d'acces
	 */
	private String accessToken;
	
	/**
	 * La durée de validité
	 */
	private long expiresIn;
	
	/**
	 * Le refresh token
	 */
	private String refreshToken;

	/**
	 * @return the accessToken
	 */
	@SerializedName("access_token")
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
	@SerializedName("expires_in")
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
	@SerializedName("refresh_token")
	public String getRefreshToken() {
		return refreshToken;
	}

	/**
	 * @param refreshToken the refreshToken to set
	 */
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
