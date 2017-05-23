package com.github.lhervier.domino.oauth.library.client.bean;

import lotus.domino.NotesException;

import com.github.lhervier.domino.oauth.common.utils.DominoUtils;

public class InitParamsBean {

	/**
	 * L'id client de l'appli
	 */
	private String clientId;
	
	/**
	 * Le secret de l'appli
	 */
	private String secret;

	/**
	 * L'URI de base
	 */
	private String baseURI;
	
	/**
	 * End point authorize
	 */
	private String authorizeEndPoint;
	
	/**
	 * end point token
	 */
	private String tokenEndPoint;
	
	/**
	 * Est ce qu'il faut désactiver la vérification du nom d'hôte dans
	 * les certificats SSL lors de l'appel du endpoint token avec un code autorisation
	 */
	private boolean disableHostNameVerifier = false;
	
	/**
	 * Constructeur
	 */
	public InitParamsBean() throws NotesException {
		DominoUtils.loadParamFromSigner(this, "Params", "INIT_");
	}
	
	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the secret
	 */
	public String getSecret() {
		return secret;
	}

	/**
	 * @param secret the secret to set
	 */
	public void setSecret(String secret) {
		this.secret = secret;
	}

	/**
	 * @return the baseURI
	 */
	public String getBaseURI() {
		return baseURI;
	}

	/**
	 * @param baseURI the baseURI to set
	 */
	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	/**
	 * @return the authorizeEndPoint
	 */
	public String getAuthorizeEndPoint() {
		return authorizeEndPoint;
	}

	/**
	 * @param authorizeEndPoint the authorizeEndPoint to set
	 */
	public void setAuthorizeEndPoint(String authorizeEndPoint) {
		this.authorizeEndPoint = authorizeEndPoint;
	}

	/**
	 * @return the tokenEndPoint
	 */
	public String getTokenEndPoint() {
		return tokenEndPoint;
	}

	/**
	 * @param tokenEndPoint the tokenEndPoint to set
	 */
	public void setTokenEndPoint(String tokenEndPoint) {
		this.tokenEndPoint = tokenEndPoint;
	}

	/**
	 * @return the disableHostNameVerifier
	 */
	public boolean isDisableHostNameVerifier() {
		return disableHostNameVerifier;
	}

	/**
	 * @param disableHostNameVerifier the disableHostNameVerifier to set
	 */
	public void setDisableHostNameVerifier(boolean disableHostNameVerifier) {
		this.disableHostNameVerifier = disableHostNameVerifier;
	}
}
