package com.github.lhervier.domino.oauth.library.client.model;

public class InitResponse {

	private String accessToken;
	
	private String refreshToken;
	
	private IdToken idToken;

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public IdToken getIdToken() {
		return idToken;
	}

	public void setIdToken(IdToken idToken) {
		this.idToken = idToken;
	}
}
