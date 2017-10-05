package com.github.lhervier.domino.oauth.server.ext.openid;


public class IdToken extends OpenIdContext {

	/**
	 * Issued date
	 */
	private long iat;

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
}
