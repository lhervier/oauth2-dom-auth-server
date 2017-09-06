package com.github.lhervier.domino.oauth.library.client.bean;

public interface InitParamsBean {

	/**
	 * @return the clientId
	 */
	public String getClientId();

	/**
	 * @return the secret
	 */
	public String getSecret();

	/**
	 * @return the baseURI
	 */
	public String getBaseURI();

	/**
	 * @return the authorizeEndPoint
	 */
	public String getAuthorizeEndPoint();

	/**
	 * @return the tokenEndPoint
	 */
	public String getTokenEndPoint();

	/**
	 * @return the disableHostNameVerifier
	 */
	public boolean isDisableHostNameVerifier();
}
