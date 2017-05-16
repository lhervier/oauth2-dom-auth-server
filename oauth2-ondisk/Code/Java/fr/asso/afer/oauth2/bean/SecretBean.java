package fr.asso.afer.oauth2.bean;

import java.io.IOException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;
import fr.asso.afer.oauth2.utils.DominoUtils;
import fr.asso.afer.oauth2.utils.JSFUtils;

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
	 * Le décodeur base64
	 */
	private static final BASE64Decoder BASE64DECODER = new BASE64Decoder();
	
	/**
	 * L'encodeur base64
	 */
	private static final BASE64Encoder BASE64ENCODER = new BASE64Encoder();
	
	/**
	 * La bean pour accéder aux paramètres
	 */
	private ParamsBean paramsBean;
	
	/**
	 * Constructeur
	 */
	public SecretBean() {
		this.paramsBean = JSFUtils.getParamsBean();
	}
	
	/**
	 * Retourne la session
	 * @return la session
	 */
	private Session getSession() {
		return JSFUtils.getSessionAsSigner();
	}
	
	/**
	 * Retourne le document config SSO
	 * @param config le nom de la config à extraire
	 * @return la config SSO
	 * @throws NotesException en cas de pb
	 */
	private Document getSsoConfig(String config) throws NotesException {
		Database names = DominoUtils.openDatabase(this.getSession(), this.paramsBean.getNab());
		if( names == null )
			throw new RuntimeException("Je n'arrive pas à accéder à la base " + this.paramsBean.getNab());
		
		View v = names.getView(WEBSSOCONFIG_VIEW);
		if( v == null )
			throw new RuntimeException("La vue " + WEBSSOCONFIG_VIEW + " n'existe pas dans la base '" + this.paramsBean.getNab() + "'. Impossible de continuer.");
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
	private byte[] getSecret(String base64, int size) throws IOException {
		byte[] min = BASE64DECODER.decodeBuffer(base64);
		byte[] ret = new byte[size];
		for( int nb = 0; nb < size; nb++ )
			ret[nb] = min[nb % min.length];
		return ret;
	}
	
	/**
	 * Retourne le secret utilisé pour signer l'access token
	 * @throws IOException 
	 */
	public byte[] getAccessTokenSecret() throws NotesException, IOException {
		String secret = this.getSsoConfig(this.paramsBean.getAccessTokenConfig()).getItemValueString(SECRET_FIELD_NAME);
		return this.getSecret(secret, 32);
	}
	
	/**
	 * Retourne le secret en base 64
	 * @throws IOException 
	 * @throws NotesException 
	 */
	public String getAccessTokenSecretBase64() throws NotesException, IOException {
		return BASE64ENCODER.encode(this.getAccessTokenSecret());
	}
	
	/**
	 * Retourne le secret utilisé pour crypter le refresh token
	 * @throws IOException 
	 */
	public byte[] getRefreshTokenSecret() throws NotesException, IOException {
		String secret = this.getSsoConfig(this.paramsBean.getRefreshTokenConfig()).getItemValueString(SECRET_FIELD_NAME);
		return this.getSecret(secret, 16);
	}
	
	/**
	 * Retourne le secret en base 64
	 * @throws IOException 
	 * @throws NotesException 
	 */
	public String getRefreshTokenSecretBase64() throws NotesException, IOException {
		return BASE64ENCODER.encode(this.getRefreshTokenSecret());
	}
}
