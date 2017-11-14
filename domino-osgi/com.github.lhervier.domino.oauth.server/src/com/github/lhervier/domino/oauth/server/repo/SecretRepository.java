package com.github.lhervier.domino.oauth.server.repo;

import java.io.IOException;

import lotus.domino.NotesException;

/**
 * Registre pour mémoriser les secrets
 * @author Lionel HERVIER
 */
public interface SecretRepository {

	/**
	 * Retourne un secret pour signer
	 * @param ssoConfig la config sso
	 */
	public byte[] findSignSecret(String ssoConfig) throws NotesException, IOException;
	
	/**
	 * Retourne un secret pour crypter
	 * @param ssoConfig la config sso
	 */
	public byte[] findCryptSecret(String ssoConfig) throws NotesException, IOException;
	
	/**
	 * Retourne le secret utilisé pour crypter le refresh token
	 * @throws IOException 
	 */
	public byte[] findRefreshTokenSecret() throws NotesException, IOException;
}
