package com.github.lhervier.domino.oauth.library.server.model;

/**
 * Un token pour rafraîchir
 * @author Lionel HERVIER
 */
public class RefreshToken {

	/**
	 * Le code autorisation
	 */
	private AuthorizationCode authCode;
	
	/**
	 * La date d'expiration
	 */
	private long exp;

	/**
	 * @return the authCode
	 */
	public AuthorizationCode getAuthCode() {
		return authCode;
	}

	/**
	 * @param authCode the authCode to set
	 */
	public void setAuthCode(AuthorizationCode authCode) {
		this.authCode = authCode;
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
