package com.github.lhervier.domino.oauth.library.server.bean;

import java.io.IOException;

import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;

import com.github.lhervier.domino.oauth.common.utils.Base64Utils;
import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.github.lhervier.domino.oauth.library.server.ServerContext;

/**
 * Registre pour mémoriser les secrets
 * @author Lionel HERVIER
 */
public class SecretBean {

	/**
	 * Le nom de la vue qui contient les configs SSO
	 */
	public static final String WEBSSOCONFIG_VIEW = "($WebSSOConfigs)";
	
	/**
	 * Le nom du champ dans lequel récupérer le secret
	 */
	public static final String SECRET_FIELD_NAME = "LTPA_DominoSecret";
	
	/**
	 * La bean pour accéder aux paramètres
	 */
	private ParamsBean paramsBean;
	
	/**
	 * The server context
	 */
	private ServerContext serverContext;
	
	/**
	 * Retourne le document config SSO
	 * @param config le nom de la config à extraire
	 * @return la config SSO
	 * @throws NotesException en cas de pb
	 */
	private Document getSsoConfig(String config) throws NotesException {
		View v = this.serverContext.getServerNab().getView(WEBSSOCONFIG_VIEW);
		if( v == null )
			throw new RuntimeException("La vue " + WEBSSOCONFIG_VIEW + " n'existe pas dans le NAB. Impossible de continuer.");
		Document ssoConfig = v.getDocumentByKey(config);
		if( ssoConfig == null )
			throw new RuntimeException("Je ne trouve pas la confg SSO '" + config + "'");
		
		return ssoConfig;
	}
	
	/**
	 * Retourne un secret
	 * @param base64 le secret en base 64
	 * @param size la taille finale
	 * @throws IOException 
	 */
	private byte[] genSecret(String base64, int size) throws IOException {
		byte[] min = Base64Utils.decode(base64);
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
	private byte[] getSecret(String ssoConfig, int size) throws NotesException, IOException {
		if( ssoConfig == null )
			return null;
		Document docSsoConfig = null;
		try {
			docSsoConfig = this.getSsoConfig(ssoConfig);
			if( docSsoConfig == null )
				return null;
			String secret = docSsoConfig.getItemValueString(SECRET_FIELD_NAME);
			return this.genSecret(secret, size);
		} finally {
			DominoUtils.recycleQuietly(docSsoConfig);
		}
	}
	
	/**
	 * Retourne un secret pour signer
	 * @param ssoConfig la config sso
	 */
	public byte[] getSignSecret(String ssoConfig) throws NotesException, IOException {
		return this.getSecret(ssoConfig, 32);
	}
	
	/**
	 * @param ssoConfig
	 */
	public String getSignSecretBase64(String ssoConfig) throws NotesException, IOException {
		return Base64Utils.encode(this.getSignSecret(ssoConfig));
	}
	
	/**
	 * Retourne un secret pour crypter
	 * @param ssoConfig la config sso
	 */
	public byte[] getCryptSecret(String ssoConfig) throws NotesException, IOException {
		return this.getSecret(ssoConfig, 16);
	}
	
	/**
	 * @param ssoConfig
	 */
	public String getCryptSecretBase64(String ssoConfig) throws NotesException, IOException {
		return Base64Utils.encode(this.getCryptSecret(ssoConfig));
	}
	
	/**
	 * Retourne le secret utilisé pour crypter le refresh token
	 * @throws IOException 
	 */
	public byte[] getRefreshTokenSecret() throws NotesException, IOException {
		return this.getCryptSecret(this.paramsBean.getRefreshTokenConfig());
	}
	
	/**
	 * Retourne le secret en base 64
	 * @throws IOException 
	 * @throws NotesException 
	 */
	public String getRefreshTokenSecretBase64() throws NotesException, IOException {
		byte[] secret = this.getRefreshTokenSecret();
		if( secret == null )
			return null;
		return Base64Utils.encode(secret);
	}
	
	// ==========================================================================

	/**
	 * @param paramsBean the paramsBean to set
	 */
	public void setParamsBean(ParamsBean paramsBean) {
		this.paramsBean = paramsBean;
	}

	/**
	 * @param serverContext the serverContext to set
	 */
	public void setServerContext(ServerContext serverContext) {
		this.serverContext = serverContext;
	}
}
