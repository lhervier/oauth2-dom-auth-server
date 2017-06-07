package com.github.lhervier.domino.oauth.library.server.model;


/**
 * Classe de base pour les tokens (refresh et access).
 * Cette classe défini un IDToken au sens OpenID :
 * 	http://openid.net/specs/openid-connect-core-1_0.html#IDToken
 * FIXME: Cette classe décrit un access_token. Pas un Idtoken openid !
 * @author Lionel HERVIER
 */
public class AccessToken {

	/**
	 * L'issuer.
	 */
	private String iss;
	
	/**
	 * L'utilisateur.
	 */
	private String sub;
	
	/**
	 * L'audiance
	 */
	private String aud;
	
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
