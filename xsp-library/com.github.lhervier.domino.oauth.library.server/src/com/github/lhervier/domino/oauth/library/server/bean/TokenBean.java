package com.github.lhervier.domino.oauth.library.server.bean;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.View;

import com.github.lhervier.domino.oauth.common.model.GrantResponse;
import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.github.lhervier.domino.oauth.common.utils.GsonUtils;
import com.github.lhervier.domino.oauth.common.utils.JSFUtils;
import com.github.lhervier.domino.oauth.common.utils.SystemUtils;
import com.github.lhervier.domino.oauth.library.server.ex.GrantException;
import com.github.lhervier.domino.oauth.library.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.library.server.ex.grant.InvalidClientException;
import com.github.lhervier.domino.oauth.library.server.ex.grant.InvalidGrantException;
import com.github.lhervier.domino.oauth.library.server.ex.grant.InvalidRequestException;
import com.github.lhervier.domino.oauth.library.server.ex.grant.InvalidScopeException;
import com.github.lhervier.domino.oauth.library.server.ex.grant.UnsupportedGrantTypeException;
import com.github.lhervier.domino.oauth.library.server.model.Application;
import com.github.lhervier.domino.oauth.library.server.model.AuthorizationCode;
import com.github.lhervier.domino.oauth.library.server.model.IdToken;
import com.ibm.xsp.designer.context.XSPContext;
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
	 * La base courante
	 */
	private Database database;
	
	/**
	 * La database courante (en tant que signer)
	 */
	private Database databaseAsSigner;
	
	/**
	 * Les paramètres du query string
	 */
	private Map<String, String> param;
	
	/**
	 * Le contexte utilisateur
	 */
	private XSPContext context;
	
	/**
	 * La requête http
	 */
	private HttpServletRequest request;
	
	/**
	 * La réponse http
	 */
	private HttpServletResponse response;
	
	/**
	 * Retourne la vue qui contient les codes autorisation
	 * @return la vue qui contient les codes autorisation
	 * @throws NotesException en cas de pb
	 */
	private View getAuthCodeView() throws NotesException {
		return DominoUtils.getView(this.database, VIEW_AUTHCODES);
	}
	
	/**
	 * Pour supprimer un code authorization.
	 * On se sert de la session ouverte par le signataire de la XPage car
	 * l'application n'a pas le droit.
	 * @param code le code
	 * @throws ServerErrorException
	 */
	private void removeCode(String code) throws ServerErrorException {
		View v = null;
		Document authDoc = null;
		try {
			v = DominoUtils.getView(this.databaseAsSigner, VIEW_AUTHCODES);
			authDoc = v.getDocumentByKey(code, true);
			if( authDoc == null )
				return;
			if( !authDoc.remove(true) )
				throw new RuntimeException("Je n'arrive pas à supprimer le document contenant les infos du code authorization");
		} catch (NotesException e) {
			throw new ServerErrorException(e);
		} finally {
			DominoUtils.recycleQuietly(authDoc);
			DominoUtils.recycleQuietly(v);
		}
	}
	
	// =============================================================================
	
	/**
	 * Gestion du token.
	 * @throws IOException
	 * @throws NotesException 
	 */
	public void token() throws IOException, NotesException {
		// Calcul l'objet réponse
		Object resp;
		try {
			// On doit passer par un POST
			if( !"POST".equals(this.request.getMethod()) )
				throw new InvalidRequestException();
			
			// Récupère le client id à partir de l'authentification
			Name nn = null;
			String clientId;
			try {
				String fullName = this.database.getParent().getEffectiveUserName();
				nn = this.database.getParent().createName(fullName);
				String appName = nn.getCommon();
				Application app = this.appBean.getApplicationFromName(appName);
				if( app == null )
					throw new InvalidClientException();
				clientId = app.getClientId();
			} finally {
				DominoUtils.recycleQuietly(nn);
			}
			
			// Si on a un client_id en paramètre, ça doit être le client_id courant
			if( this.param.containsKey("client_id") ) {
				if( !clientId.equals(this.param.get("client_id")) )
					throw new InvalidClientException();
			}
			
			// On doit avoir un grant_type
			String grantType = this.param.get("grant_type");
			if( grantType == null )
				throw new InvalidRequestException();
			
			// L'objet pour la réponse
			if( "authorization_code".equals(grantType) )
				resp = this.authorizationCode(
						this.param.get("code"),
						this.param.get("redirect_uri"),
						clientId
				);
			else if( "refresh_token".equals(grantType) )
				resp = this.refreshToken(
						this.param.get("refresh_token"),
						this.param.get("scope")
				);
			else
				throw new UnsupportedGrantTypeException();
		} catch(GrantException e) {
			this.response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp = e.getError();
		} catch (ServerErrorException e) {
			this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			resp = null;
		}
		
		// Envoi dans la stream http
		JSFUtils.sendJson(resp);
	}
	
	/**
	 * Génération d'un token à partir d'un code autorisation
	 * @param code le code autorisation
	 * @param redirectUri l'uri de redirection
	 * @param clientId l'id de l'appli cliente
	 * @throws GrantException en cas de pb das le grant
	 * @throws ServerErrorException en cas d'erreur pendant l'accès au serveur
	 */
	public GrantResponse authorizationCode(String code, String redirectUri, String clientId) throws GrantException, ServerErrorException {
		if( code == null )
			throw new InvalidRequestException();
		if( redirectUri == null )
			throw new InvalidRequestException();
		if( clientId == null )
			throw new InvalidRequestException();
		
		GrantResponse resp = new GrantResponse();
		
		Name nn = null;
		Document authDoc = null;
		View v = null;
		try {
			nn = this.database.getParent().createName(this.database.getParent().getEffectiveUserName());
			v = this.getAuthCodeView();
			
			// Récupère l'application
			Application app = this.appBean.getApplicationFromName(nn.getCommon());
			if( app == null )
				throw new InvalidClientException();
			
			// Récupère le document correspondant au code authorization
			authDoc = v.getDocumentByKey(code, true);
			if( authDoc == null )
				throw new InvalidGrantException();
			AuthorizationCode authCode = DominoUtils.fillObject(new AuthorizationCode(), authDoc);
			
			// Vérifie qu'il n'est pas expiré
			long expired = (long) authCode.getExpires();
			if( expired < SystemUtils.currentTimeSeconds() )
				throw new InvalidGrantException();
			
			// Vérifie que le clientId est le bon
			if( !clientId.equals(authCode.getAud()) )
				throw new InvalidClientException();
			
			// Vérifie que l'uri de redirection est la même
			if( !redirectUri.equals(authCode.getRedirectUri()) )
				throw new InvalidGrantException();
			
			// Génère le access token. Il est signé avec la clé partagée avec les serveurs de ressources.
			IdToken accessToken = DominoUtils.fillObject(new IdToken(), authDoc);
			accessToken.setExp(SystemUtils.currentTimeSeconds() + this.paramsBean.getAccessTokenLifetime());
			JWSObject jwsObject = new JWSObject(
					new JWSHeader(JWSAlgorithm.HS256),
                    new Payload(GsonUtils.toJson(accessToken))
			);
			jwsObject.sign(new MACSigner(
					this.secretBean.getAccessTokenSecret()
			));
			resp.setAccessToken(jwsObject.serialize());
			
			// Génère le refresh token
			IdToken refreshToken = DominoUtils.fillObject(new IdToken(), authDoc);
			refreshToken.setExp(SystemUtils.currentTimeSeconds() + this.paramsBean.getRefreshTokenLifetime());
			JWEObject jweObject = new JWEObject(
					new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128GCM), 
					new Payload(GsonUtils.toJson(refreshToken))
			);
			jweObject.encrypt(new DirectEncrypter(
					this.secretBean.getRefreshTokenSecret()
			));
			resp.setRefreshToken(jweObject.serialize());
			
			// La durée d'expiration. On prend celle du accessToken
			resp.setExpiresIn(accessToken.getExp() - SystemUtils.currentTimeSeconds());
			
			// Le type de token
			resp.setTokenType("Bearer");
			
			// Définit les scopes s'il sont différents de ceux demandés lors de la requête à Authorize
			if( !authCode.getScopes().containsAll(authCode.getGrantedScopes()) )
				resp.setScopes(authCode.getGrantedScopes());
			
			return resp;
		} catch (NotesException e) {
			throw new ServerErrorException(e);
		} catch (KeyLengthException e) {
			throw new ServerErrorException(e);
		} catch (JOSEException e) {
			throw new ServerErrorException(e);
		} catch (IOException e) {
			throw new ServerErrorException(e);
		} finally {
			DominoUtils.recycleQuietly(nn);
			DominoUtils.recycleQuietly(authDoc);
			DominoUtils.recycleQuietly(v);
			
			// Supprime le code authorization pour empêcher une ré-utilisation
			this.removeCode(code);
		}
	}
	
	/**
	 * Génération d'un token à partir d'un refresh token
	 * @param sRefreshToken le refresh token
	 * @param scope un éventuel nouveau scope.
	 * @throws GrantException 
	 * @throws ServerErrorException
	 */
	public GrantResponse refreshToken(String sRefreshToken, String scope) throws GrantException, ServerErrorException {
		if( sRefreshToken == null )
			throw new InvalidGrantException();
		
		try {
			// Décrypte le refresh token
			JWEObject jweObject = JWEObject.parse(sRefreshToken);
			jweObject.decrypt(new DirectDecrypter(this.secretBean.getRefreshTokenSecret()));
			String json = jweObject.getPayload().toString();
			IdToken refreshToken = GsonUtils.fromJson(json, IdToken.class);
			
			// FIXME: Vérifie que les scopes demandés sont bien dans la liste des scopes déjà accordés
			if( scope != null ) {
				if( scope.length() != 0 )			// C'est le seul scope que l'on fournisse aujourd'hui...
					throw new InvalidScopeException();
			}
			
			// Vérifie qu'il est valide
			if( refreshToken.getExp() < SystemUtils.currentTimeSeconds() )
				throw new InvalidGrantException();
			
			// Vérifie qu'il existe bien une application pour ce login
			String appName = this.context.getUser().getCommonName();
			Application app = this.appBean.getApplicationFromName(appName);
			if( app == null )
				throw new InvalidGrantException();
			
			// Vérifie que le token a bien été généré pour cette application
			if( !app.getClientId().equals(refreshToken.getAud()) )
				throw new InvalidGrantException();
			
			// Prolonge la durée de vie du refresh token
			refreshToken.setExp(SystemUtils.currentTimeSeconds() + this.paramsBean.getRefreshTokenLifetime());		// 10 heures
			
			// Génère l'access token
			IdToken accessToken = new IdToken();
			accessToken.setExp(SystemUtils.currentTimeSeconds() + this.paramsBean.getAccessTokenLifetime());
			accessToken.setAud(refreshToken.getAud());
			accessToken.setAuthTime(refreshToken.getAuthTime());
			accessToken.setIat(refreshToken.getIat());
			accessToken.setIss(refreshToken.getIss());
			accessToken.setSub(refreshToken.getSub());
			
			// Créé les jwt et jwe
			JWSObject jwsObject = new JWSObject(
					new JWSHeader(JWSAlgorithm.HS256),
	                new Payload(GsonUtils.toJson(accessToken))
			);
			jwsObject.sign(new MACSigner(
					this.secretBean.getAccessTokenSecret()
			));
			jweObject = new JWEObject(
					new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128GCM), 
					new Payload(GsonUtils.toJson(refreshToken))
			);
			jweObject.encrypt(new DirectEncrypter(
					this.secretBean.getRefreshTokenSecret()
			));
			
			GrantResponse resp = new GrantResponse();
			resp.setAccessToken(jwsObject.serialize());
			resp.setRefreshToken(jweObject.serialize());
			resp.setExpiresIn(accessToken.getExp());		// Expiration en même temps que le refresh token
			
			return resp;
		} catch (ParseException e) {
			throw new ServerErrorException(e);
		} catch (KeyLengthException e) {
			throw new ServerErrorException(e);
		} catch (NotesException e) {
			throw new ServerErrorException(e);
		} catch (JOSEException e) {
			throw new ServerErrorException(e);
		} catch (IOException e) {
			throw new ServerErrorException(e);
		}
	}
	
	// =========================================================================================================

	/**
	 * @param database the database to set
	 */
	public void setDatabase(Database database) {
		this.database = database;
	}

	/**
	 * @param appBean the appBean to set
	 */
	public void setAppBean(AppBean appBean) {
		this.appBean = appBean;
	}

	/**
	 * @param secretBean the secretBean to set
	 */
	public void setSecretBean(SecretBean secretBean) {
		this.secretBean = secretBean;
	}

	/**
	 * @param paramsBean the paramsBean to set
	 */
	public void setParamsBean(ParamsBean paramsBean) {
		this.paramsBean = paramsBean;
	}

	/**
	 * @param param the param to set
	 */
	public void setParam(Map<String, String> param) {
		this.param = param;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(XSPContext context) {
		this.context = context;
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	/**
	 * @param databaseAsSigner the databaseAsSigner to set
	 */
	public void setDatabaseAsSigner(Database databaseAsSigner) {
		this.databaseAsSigner = databaseAsSigner;
	}

	/**
	 * @param request the request to set
	 */
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
}
