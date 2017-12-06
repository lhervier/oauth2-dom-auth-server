package com.github.lhervier.domino.oauth.server.ext.openid;

public class OpenIDContext {

	/**
	 * Authenticated time
	 */
	private long authTime;

	/**
	 * Nonce
	 */
	private String nonce;
	
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
	 * @return the nonce
	 */
	public String getNonce() {
		return nonce;
	}

	/**
	 * @param nonce the nonce to set
	 */
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
}
