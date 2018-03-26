package com.github.lhervier.domino.oauth.server.repo.impl.notes;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.github.lhervier.domino.oauth.server.notes.AuthContext;
import com.github.lhervier.domino.oauth.server.notes.DominoUtils;
import com.github.lhervier.domino.oauth.server.notes.NotesRuntimeException;
import com.github.lhervier.domino.oauth.server.repo.SecretRepository;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;

/**
 * Registre pour mémoriser les secrets
 * @author Lionel HERVIER
 */
@Repository
public class NotesSecretRepository implements SecretRepository {

	/**
	 * Le nom de la vue qui contient les configs SSO
	 */
	public static final String WEBSSOCONFIG_VIEW = "($WebSSOConfigs)";
	
	/**
	 * Le nom du champ dans lequel récupérer le secret
	 */
	public static final String SECRET_FIELD_NAME = "LTPA_DominoSecret";
	
	/**
	 * The notes context
	 */
	@Autowired
	private AuthContext authContext;
	
	/**
	 * Retourne un secret
	 * @param base64 le secret en base 64
	 * @param size la taille finale
	 * @throws IOException 
	 */
	private byte[] genSecret(String base64, int size) throws IOException {
		byte[] min = Base64.decodeBase64(base64.getBytes("UTF-8"));
		byte[] ret = new byte[size];
		for( int nb = 0; nb < size; nb++ )
			ret[nb] = min[nb % min.length];
		return ret;
	}
	
	/**
	 * Retourne un secret
	 * @param ssoConfig la config sso
	 * @param size la taille
	 */
	private byte[] getSecret(String ssoConfig, int size) {
		if( ssoConfig == null )
			return null;
		Database nab = null;
		View v = null;
		Document docSsoConfig = null;
		try {
			nab = DominoUtils.openDatabase(this.authContext.getServerSession(), "names.nsf");
			v = nab.getView(WEBSSOCONFIG_VIEW);
			if( v == null )
				throw new NotesRuntimeException("La vue " + WEBSSOCONFIG_VIEW + " n'existe pas dans le NAB. Impossible de continuer.");
			docSsoConfig = v.getDocumentByKey(ssoConfig);
			if( docSsoConfig == null )
				return null;
			String secret = docSsoConfig.getItemValueString(SECRET_FIELD_NAME);
			return this.genSecret(secret, size);
		} catch(NotesException e) {
			throw new NotesRuntimeException("Error extracting secret", e);
		} catch(IOException e) {
			throw new NotesRuntimeException("Error extracting secret", e);
		} finally {
			DominoUtils.recycleQuietly(docSsoConfig);
			DominoUtils.recycleQuietly(v);
			DominoUtils.recycleQuietly(nab);
		}
	}
	
	// ==================================================================================
	
	/**
	 * Retourne un secret pour signer
	 * @param ssoConfig la config sso
	 */
	public byte[] findSignSecret(String ssoConfig) {
		return this.getSecret(ssoConfig, 32);
	}
	
	/**
	 * Retourne un secret pour crypter
	 * @param ssoConfig la config sso
	 */
	public byte[] findCryptSecret(String ssoConfig) {
		return this.getSecret(ssoConfig, 16);
	}
}
