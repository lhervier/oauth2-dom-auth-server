package com.github.lhervier.domino.oauth.server.ext.core;

public class OpenIDContext {

	/**
	 * ID Token issued date
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
