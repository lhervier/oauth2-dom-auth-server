package fr.asso.afer.rest.sample.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Un token tel que passé dans l'en tête authorization
 * @author Lionel HERVIER
 */
public class AccessToken {

	/**
	 * L'issuer
	 */
	private String iss;
	
	/**
	 * L'utilisateur
	 */
	private String sub;
	
	/**
	 * L'audiance
	 */
	private String aud;
	
	/**
	 * La date a laquelle le token a été généré
	 */
	private long iat;
	
	/**
	 * La date à laquelle l'utilisateur s'est authentifié
	 */
	@JsonProperty("auth_time")
	private long authTime;
	
	/**
	 * La date d'expiration
	 */
	private long exp;

	/**
	 * @return the iss
	 */
	public String getIss() {
		return iss;
	}

	/**
	 * @param iss the iss to set
	 */
	public void setIss(String iss) {
		this.iss = iss;
	}

	/**
	 * @return the sub
	 */
	public String getSub() {
		return sub;
	}

	/**
	 * @param sub the sub to set
	 */
	public void setSub(String sub) {
		this.sub = sub;
	}

	/**
	 * @return the aud
	 */
	public String getAud() {
		return aud;
	}

	/**
	 * @param aud the aud to set
	 */
	public void setAud(String aud) {
		this.aud = aud;
	}

	/**
	 * @return the iat
	 */
	public long getIat() {
		return iat;
	}

	/**
	 * @param iat the iat to set
	 */
	public void setIat(long iat) {
		this.iat = iat;
	}

	/**
	 * @return the authTime
	 */
	public long getAuthTime() {
		return authTime;
	}

	/**
	 * @param authTime the authTime to set
	 */
	public void setAuthTime(long authTime) {
		this.authTime = authTime;
	}

	/**
	 * @return the exp
	 */
	public long getExp() {
		return exp;
	}

	/**
	 * @param exp the exp to set
	 */
	public void setExp(long exp) {
		this.exp = exp;
	}
}
