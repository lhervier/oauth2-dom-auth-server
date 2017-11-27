package com.github.lhervier.domino.oauth.server.repo;

/**
 * Registre pour mémoriser les secrets
 * @author Lionel HERVIER
 */
public interface SecretRepository {

	/**
	 * Retourne un secret pour signer
	 * @param ssoConfig la config sso
	 */
	public byte[] findSignSecret(String ssoConfig);
	
	/**
	 * Retourne un secret pour crypter
	 * @param ssoConfig la config sso
	 */
	public byte[] findCryptSecret(String ssoConfig);
}
