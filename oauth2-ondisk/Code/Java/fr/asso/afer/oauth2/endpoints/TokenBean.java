package fr.asso.afer.oauth2.endpoints;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;

import fr.asso.afer.oauth2.app.AppBean;
import fr.asso.afer.oauth2.model.AccessToken;
import fr.asso.afer.oauth2.model.Application;
import fr.asso.afer.oauth2.model.GrantResponse;
import fr.asso.afer.oauth2.model.RefreshToken;
import fr.asso.afer.oauth2.params.ParamsBean;
import fr.asso.afer.oauth2.secret.SecretBean;
import fr.asso.afer.oauth2.utils.DominoUtils;
import fr.asso.afer.oauth2.utils.IOUtils;
import fr.asso.afer.oauth2.utils.JSFUtils;
import fr.asso.afer.oauth2.utils.JsonUtils;

/**
 * Bean pour le endpoint "token"
 * @author Lionel HERVIER
 */
public class TokenBean {

	/**
	 * Le nom de la vue qui contient les codes autorization
	 */
	private static final String VIEW_AUTHCODES = "AuthorizationCodes";
	
	/**
	 * La session
	 */
	private Session session;
	
	/**
	 * La session en tant que le signataire de la XPage
	 */
	private Session sessionAsSigner;
	
	/**
	 * La base courante
	 */
	private Database database;
	
	/**
	 * La vue des codes authorization
	 */
	private View v;
	
	/**
	 * La bean pour gérer les apps
	 */
	private AppBean appBean;
	
	/**
	 * La bean pour accéder aux secrets
	 */
	private SecretBean secretBean;
	
	/**
	 * La bean pour accéder aux paramètres
	 */
	private ParamsBean paramsBean;
	
	/**
	 * Constructeur
	 * @throws NotesException en cas de pb
	 */
	public TokenBean() throws NotesException {
		this.session = JSFUtils.getSession();
		this.sessionAsSigner = JSFUtils.getSessionAsSigner();
		this.database = JSFUtils.getDatabase();
		this.appBean = JSFUtils.getAppBean();
		this.secretBean = JSFUtils.getSecretBean();
		this.paramsBean = JSFUtils.getParamsBean();
		this.v = DominoUtils.getView(this.database, VIEW_AUTHCODES);
	}
	
	/**
	 * Pour supprimer un code authorization.
	 * On se sert de la session ouverte par le signataire de la XPage car
	 * l'application n'a pas le droit.
	 * @param code le code
	 * @throws NotesException 
	 */
	private void removeCode(String code) throws NotesException {
		Database db = null;
		View v = null;
		Document authDoc = null;
		try {
			db = DominoUtils.openDatabase(this.sessionAsSigner, this.database.getFilePath());
			v = DominoUtils.getView(db, VIEW_AUTHCODES);
			authDoc = v.getDocumentByKey(code, true);
			if( authDoc == null )
				throw new RuntimeException("Erreur à la suppression du document contenant les infos du code authorization");
			if( !authDoc.remove(true) )
				throw new RuntimeException("Je n'arrive pas à supprimer le document contenant les infos du code authorization");
		} finally {
			DominoUtils.recycleQuietly(authDoc);
			DominoUtils.recycleQuietly(v);
			DominoUtils.recycleQuietly(db);
		}
	}
	
	// =============================================================================
	
	/**
	 * Gestion du token.
	 * @param out la stream dans laquelle envoyer la réponse
	 */
	public void token(HttpServletResponse response) {
		OutputStream out = null;
		OutputStreamWriter wrt = null;
		try {
			Map<String, String> param = JSFUtils.getParam();
			String grantType = param.get("grant_type");
			
			// L'objet pour la réponse
			Object resp;
			if( "authorization_code".equals(grantType) )
				resp = this.authorizationCode();
			else if( "refresh_token".equals(grantType) )
				resp = this.refreshToken();
			else
				throw new RuntimeException("Type de grant '" + grantType + "' inconnu...");
			
			// Envoi dans la stream http
			response.setContentType("application/json;charset=UTF-8");
			response.setHeader("Cache-Control", "no-store");
			response.setHeader("Pragma", "no-cache");
			out = response.getOutputStream();
			wrt = new OutputStreamWriter(out, "UTF-8");
			wrt.write(JsonUtils.toJson(resp));
		} catch(Throwable e) {
			e.printStackTrace(System.err);
		} finally {
			IOUtils.closeQuietly(wrt);
			IOUtils.closeQuietly(out);
		}
	}
	
	/**
	 * Génération d'un token à partir d'un code autorisation
	 * @throws NotesException en cas de pb
	 * @throws JOSEException 
	 * @throws KeyLengthException 
	 * @throws IOException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IntrospectionException 
	 * @throws IllegalArgumentException 
	 */
	public GrantResponse authorizationCode() throws NotesException, KeyLengthException, JOSEException, IOException, IllegalArgumentException, IntrospectionException, IllegalAccessException, InvocationTargetException {
		GrantResponse resp = new GrantResponse();
		
		Map<String, String> param = JSFUtils.getParam();
		String code = param.get("code");
		String redirectUri = param.get("redirect_uri");
		String clientId = param.get("client_id");
		
		Name nn = null;
		Document authDoc = null;
		try {
			nn = this.session.createName(this.session.getEffectiveUserName());
			
			// Récupère l'application
			Application app = this.appBean.getApplicationFromName(nn.getCommon());
			if( app == null )
				throw new RuntimeException("Application '" + nn.getCommon() + "' inconnue...");
			
			// Récupère le document correspondant au code authorization
			authDoc = this.v.getDocumentByKey(code, true);
			if( authDoc == null )
				throw new RuntimeException("Code autorisation incorrect");
			
			// Vérifie qu'il n'est pas expiré
			long expired = (long) authDoc.getItemValueDouble("accessExp");
			if( expired < System.currentTimeMillis() )
				throw new RuntimeException("Code autorisation incorrect");
			
			// Vérifie que le clientId est le bon
			if( !clientId.equals(authDoc.getItemValueString("aud")) )
				throw new RuntimeException("Code autorisation incorrect");
			
			// Vérifie que l'uri de redirection est la même
			if( !redirectUri.equals(authDoc.getItemValueString("RedirectUri")) )
				throw new RuntimeException("Code autorisation incorrect");
			
			// Génère le access token. Il est signé avec la clé partagée avec les serveurs de ressources.
			AccessToken accessToken = new AccessToken();
			DominoUtils.fillObject(accessToken, authDoc);
			accessToken.setAccessExp(System.currentTimeMillis() + this.paramsBean.getAccessTokenLifetime());
			JWSObject jwsObject = new JWSObject(
					new JWSHeader(JWSAlgorithm.HS256),
                    new Payload(JsonUtils.toJson(accessToken))
			);
			jwsObject.sign(new MACSigner(
					this.secretBean.getAccessTokenSecret()
			));
			resp.setAccessToken(jwsObject.serialize());
			
			// Génère le refresh token
			RefreshToken refreshToken = new RefreshToken();
			DominoUtils.fillObject(refreshToken, authDoc);
			refreshToken.setRefreshExp(System.currentTimeMillis() + this.paramsBean.getRefreshTokenLifetime());
			JWEObject jweObject = new JWEObject(
					new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128GCM), 
					new Payload(JsonUtils.toJson(refreshToken))
			);
			jweObject.encrypt(new DirectEncrypter(
					this.secretBean.getRefreshTokenSecret()
			));
			resp.setRefreshToken(jweObject.serialize());
			
			// La durée d'expiration. On prend celle du accessToken
			resp.setExpiresIn(accessToken.getAccessExp() - System.currentTimeMillis());
			
			return resp;		
		} finally {
			DominoUtils.recycleQuietly(nn);
			DominoUtils.recycleQuietly(authDoc);
			
			// Supprime le code authorization pour empêcher une ré-utilisation
			this.removeCode(code);
		}
	}
	
	/**
	 * Génération d'un token à partir d'un refresh token
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws JOSEException 
	 * @throws NotesException 
	 * @throws KeyLengthException 
	 * @throws InvocationTargetException 
	 * @throws IntrospectionException 
	 * @throws InstantiationException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public GrantResponse refreshToken() throws ParseException, KeyLengthException, NotesException, JOSEException, IOException, IllegalArgumentException, IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {
		String sRefreshToken = JSFUtils.getParam().get("refresh_token");
		if( sRefreshToken == null )
			throw new RuntimeException("Le refresh_token est vide");
		
		// Décrypte le refresh token
		JWEObject jweObject = JWEObject.parse(sRefreshToken);
		jweObject.decrypt(new DirectDecrypter(this.secretBean.getRefreshTokenSecret()));
		String json = jweObject.getPayload().toString();
		RefreshToken refreshToken = JsonUtils.fromJson(json, RefreshToken.class);
		
		// Vérifie qu'il est valide
		if( refreshToken.getRefreshExp() < System.currentTimeMillis() )
			throw new RuntimeException("Refresh Token expiré");
		
		// Prolonge la durée de vie du refresh token
		refreshToken.setRefreshExp(System.currentTimeMillis() + this.paramsBean.getRefreshTokenLifetime());		// 10 heures
		
		// Génère l'access token
		AccessToken accessToken = new AccessToken();
		accessToken.setAccessExp(System.currentTimeMillis() + this.paramsBean.getAccessTokenLifetime());
		accessToken.setAud(refreshToken.getAud());
		accessToken.setAuthDate(refreshToken.getAuthDate());
		accessToken.setIat(refreshToken.getIat());
		accessToken.setIss(refreshToken.getIss());
		accessToken.setSub(refreshToken.getSub());
		
		// Créé les jwt et jwe
		JWSObject jwsObject = new JWSObject(
				new JWSHeader(JWSAlgorithm.HS256),
                new Payload(JsonUtils.toJson(accessToken))
		);
		jwsObject.sign(new MACSigner(
				this.secretBean.getAccessTokenSecret()
		));
		jweObject = new JWEObject(
				new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128GCM), 
				new Payload(JsonUtils.toJson(refreshToken))
		);
		jweObject.encrypt(new DirectEncrypter(
				this.secretBean.getRefreshTokenSecret()
		));
		
		GrantResponse resp = new GrantResponse();
		resp.setAccessToken(jwsObject.serialize());
		resp.setRefreshToken(jweObject.serialize());
		resp.setExpiresIn(accessToken.getAccessExp());		// Expiration en même temps que le refresh token
		
		return resp;
	}
	
}
