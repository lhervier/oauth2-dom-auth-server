package fr.asso.afer.oauth2.model;

import fr.asso.afer.oauth2.utils.JsonUtils.JsonName;

/**
 * Classe de base pour les tokens (refresh et access)
 * @author Lionel HERVIER
 */
public class Token {

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
	private long authTime;

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
	@JsonName("auth_time")
	public long getAuthTime() {
		return authTime;
	}

	/**
	 * @param authTime the authTime to set
	 */
	public void setAuthTime(long authDate) {
		this.authTime = authDate;
	}
}
