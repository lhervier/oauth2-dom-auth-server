package com.github.lhervier.domino.oauth.server.services;

import com.github.lhervier.domino.oauth.server.entity.AuthCodeEntity;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;

public interface AuthCodeService {

	/**
	 * Generate a new refresh token
	 */
	public String fromEntity(AuthCodeEntity authCode) throws ServerErrorException;
	
	/**
	 * Extract a refresh token
	 */
	public AuthCodeEntity toEntity(String refreshToken) throws ServerErrorException;
}
