package com.github.lhervier.domino.oauth.library.server.bean;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import lotus.domino.Document;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.View;

import org.apache.commons.lang.StringUtils;

import com.github.lhervier.domino.oauth.common.HttpContext;
import com.github.lhervier.domino.oauth.common.NotesContext;
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
import com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.library.server.ext.IScopeGranter;
import com.github.lhervier.domino.oauth.library.server.model.Application;
import com.github.lhervier.domino.oauth.library.server.model.AuthorizationCode;
import com.github.lhervier.domino.oauth.library.server.model.RefreshToken;
import com.github.lhervier.domino.oauth.library.server.utils.JsonObjectPropertyAdder;
import com.github.lhervier.domino.oauth.library.server.utils.Utils;
import com.google.gson.JsonObject;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;

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
	 * The notes context
	 */
	private NotesContext notesContext;
	
	/**
	 * The http context
	 */
	private HttpContext httpContext;
	
	/**
	 * Retourne la vue qui contient les codes autorisation
	 * @return la vue qui contient les codes autorisation
	 * @throws NotesException en cas de pb
	 */
	private View getAuthCodeView() throws NotesException {
		return DominoUtils.getView(this.notesContext.getUserDatabase(), VIEW_AUTHCODES);
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
			v = DominoUtils.getView(this.notesContext.getServerDatabase(), VIEW_AUTHCODES);
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
			if( !"POST".equals(this.httpContext.getRequest().getMethod()) )
				throw new InvalidRequestException();
			
			// Récupère le client id à partir de l'authentification
			Name nn = null;
			String clientId;
			try {
				String fullName = this.notesContext.getUserDatabase().getParent().getEffectiveUserName();
				nn = this.notesContext.getUserDatabase().getParent().createName(fullName);
				String appName = nn.getCommon();
				Application app = this.appBean.getApplicationFromName(appName);
				if( app == null )
					throw new InvalidClientException();
				clientId = app.getClientId();
			} finally {
				DominoUtils.recycleQuietly(nn);
			}
			
			// Si on a un client_id en paramètre, ça doit être le client_id courant
			if( !StringUtils.isEmpty(this.httpContext.getRequest().getParameter("client_id")) ) {
				if( !clientId.equals(this.httpContext.getRequest().getParameter("client_id")) )
					throw new InvalidClientException();
			}
			
			// On doit avoir un grant_type
			String grantType = this.httpContext.getRequest().getParameter("grant_type");
			if( StringUtils.isEmpty(grantType) )
				throw new InvalidRequestException();
			
			// L'objet pour la réponse
			if( "authorization_code".equals(grantType) )
				resp = this.authorizationCode(
						this.httpContext.getRequest().getParameter("code"),
						this.httpContext.getRequest().getParameter("redirect_uri"),
						clientId
				);
			else if( "refresh_token".equals(grantType) ) {
				List<String> scopes;
				if( StringUtils.isEmpty(this.httpContext.getRequest().getParameter("scope")) )
					scopes = new ArrayList<String>();
				else
					scopes = Arrays.asList(StringUtils.split(this.httpContext.getRequest().getParameter("scope"), " "));
				resp = this.refreshToken(
						this.httpContext.getRequest().getParameter("refresh_token"),
						scopes
				);
			} else
				throw new UnsupportedGrantTypeException();
		} catch(GrantException e) {
			this.httpContext.getResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp = e.getError();
		} catch (ServerErrorException e) {
			this.httpContext.getResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			resp = null;
		}
		
		// Envoi dans la stream http
		JSFUtils.sendJson(this.httpContext.getResponse(), resp);
	}
	
	/**
	 * Créé un refresh token
	 * @param authCode le code autorisation
	 * @return le refresh token
	 * @throws IOException 
	 * @throws JOSEException 
	 * @throws NotesException 
	 * @throws KeyLengthException 
	 */
	private String createRefreshToken(AuthorizationCode authCode) throws KeyLengthException, NotesException, JOSEException, IOException {
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setAuthCode(authCode);
		refreshToken.setExp(SystemUtils.currentTimeSeconds() + this.paramsBean.getRefreshTokenLifetime());
		JWEObject jweObject = new JWEObject(
				new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128GCM), 
				new Payload(GsonUtils.toJson(refreshToken))
		);
		jweObject.encrypt(new DirectEncrypter(
				this.secretBean.getRefreshTokenSecret()
		));
		return jweObject.serialize();
	}
	
	/**
	 * Génération d'un token à partir d'un code autorisation
	 * @param code le code autorisation
	 * @param redirectUri l'uri de redirection
	 * @param clientId l'id de l'appli cliente
	 * @throws GrantException en cas de pb das le grant
	 * @throws ServerErrorException en cas d'erreur pendant l'accès au serveur
	 */
	public JsonObject authorizationCode(String code, String redirectUri, String clientId) throws GrantException, ServerErrorException {
		if( code == null )
			throw new InvalidRequestException();
		if( redirectUri == null )
			throw new InvalidRequestException();
		if( clientId == null )
			throw new InvalidRequestException();
		
		JsonObject resp = new JsonObject();
		
		Name nn = null;
		Document authDoc = null;
		View v = null;
		try {
			nn = this.notesContext.getUserSession().createName(this.notesContext.getUserSession().getEffectiveUserName());
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
			if( !clientId.equals(authCode.getClientId()) )
				throw new InvalidClientException();
			
			// Vérifie que l'uri de redirection est la même
			if( !redirectUri.equals(authCode.getRedirectUri()) )
				throw new InvalidGrantException();
			
			// Fait générer les propriétés supplémentaires par chaque plugin
			// Ils peuvent modifier leur contexte.
			List<IOAuthExtension> exts = Utils.getExtensions();
			for( IOAuthExtension ext : exts ) {
				JsonObject context = (JsonObject) authCode.getContexts().get(ext.getId());
				if( context == null )
					continue;
				JsonObject jsonConf = this.paramsBean.getPluginConf(ext.getId());
				ext.token(
						this.httpContext,
						this.notesContext,
						jsonConf,
						context, 
						new JsonObjectPropertyAdder(
								resp, 
								this.secretBean,
								jsonConf.get("sign_key").isJsonNull() ? null : jsonConf.get("sign_key").getAsString(),
								jsonConf.get("crypt_key").isJsonNull() ? null : jsonConf.get("crypt_key").getAsString()
						)
				);
			}
			
			// Génère le refresh token
			String refreshToken = this.createRefreshToken(authCode);
			resp.addProperty("refresh_token", refreshToken);
			
			// La durée d'expiration.
			resp.addProperty("expires_in", this.paramsBean.getRefreshTokenLifetime());
			
			// Le type de token
			resp.addProperty("token_type", "Bearer");
			
			// Définit les scopes s'il sont différents de ceux demandés lors de la requête à Authorize
			if( !authCode.getScopes().containsAll(authCode.getGrantedScopes()) )
				resp.addProperty("scope", StringUtils.join(authCode.getGrantedScopes().iterator(), " "));
			
			return resp;
		} catch (NotesException e) {
			e.printStackTrace(System.err);				// FIXME: Où envoyer ça ?
			throw new ServerErrorException(e);
		} catch (KeyLengthException e) {
			e.printStackTrace(System.err);				// FIXME: Où envoyer ça ?
			throw new ServerErrorException(e);
		} catch (JOSEException e) {
			e.printStackTrace(System.err);				// FIXME: Où envoyer ça ?
			throw new ServerErrorException(e);
		} catch (IOException e) {
			e.printStackTrace(System.err);				// FIXME: Où envoyer ça ?
			throw new ServerErrorException(e);
		} catch(Throwable e) {
			e.printStackTrace(System.err);				// FIXME: Où envoyer ça ?
			throw new RuntimeException(e);
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
	 * @param scopes d'éventuels nouveaux scopes.
	 * @throws GrantException 
	 * @throws ServerErrorException
	 */
	public JsonObject refreshToken(String sRefreshToken, List<String> scopes) throws GrantException, ServerErrorException {
		if( sRefreshToken == null )
			throw new InvalidGrantException();
		
		Name nn = null;
		try {
			// Décrypte le refresh token
			JWEObject jweObject = JWEObject.parse(sRefreshToken);
			jweObject.decrypt(new DirectDecrypter(this.secretBean.getRefreshTokenSecret()));
			String json = jweObject.getPayload().toString();
			RefreshToken refreshToken = GsonUtils.fromJson(json, RefreshToken.class);
			
			// Vérifie que les scopes demandés sont bien dans la liste des scopes déjà accordés
			if( !refreshToken.getAuthCode().getGrantedScopes().containsAll(scopes) )
				throw new InvalidScopeException();
			
			// Vérifie qu'il est valide
			if( refreshToken.getExp() < SystemUtils.currentTimeSeconds() )
				throw new InvalidGrantException();
			
			// Vérifie qu'il existe bien une application pour ce login
			nn = this.notesContext.getUserSession().createName(this.notesContext.getUserSession().getEffectiveUserName());
			String appName = nn.getCommon();
			Application app = this.appBean.getApplicationFromName(appName);
			if( app == null )
				throw new InvalidGrantException();
			
			// Vérifie que le token a bien été généré pour cette application
			if( !app.getClientId().equals(refreshToken.getAuthCode().getClientId()) )
				throw new InvalidGrantException();
			
			// Prépare la réponse
			JsonObject resp = new JsonObject();
			
			// Fait travailler les plugins
			List<IOAuthExtension> exts = Utils.getExtensions();
			final List<String> grantedScopes = new ArrayList<String>();
			for( IOAuthExtension ext : exts ) {
				JsonObject context = (JsonObject) refreshToken.getAuthCode().getContexts().get(ext.getId());
				if( context == null )
					continue;
				JsonObject jsonConf = this.paramsBean.getPluginConf(ext.getId());
				ext.refresh(
						this.httpContext,
						this.notesContext,
						jsonConf,
						context, 
						new JsonObjectPropertyAdder(
								resp,
								this.secretBean,
								jsonConf.get("sign_key") == null ? null : jsonConf.get("sign_key").getAsString(),
								jsonConf.get("crypt_key") == null ? null : jsonConf.get("crypt_key").getAsString()
						), 
						new IScopeGranter() {
							@Override
							public void grant(String scope) {
								grantedScopes.add(scope);
							}
						},
						scopes
				);
			}
			
			// Met à jour les scopes
			if( scopes.size() != 0 ) {
				if( !grantedScopes.containsAll(refreshToken.getAuthCode().getGrantedScopes())) {
					resp.addProperty("scope", StringUtils.join(grantedScopes.iterator(), " "));
					refreshToken.getAuthCode().setGrantedScopes(grantedScopes);
				}
			}
			
			// Génère le refresh token
			String newRefreshToken = this.createRefreshToken(refreshToken.getAuthCode());
			resp.addProperty("refresh_token", newRefreshToken);
			
			// Les autres infos
			resp.addProperty("expires_in", this.paramsBean.getRefreshTokenLifetime());
			resp.addProperty("token_type", "Bearer");
			
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
		} finally {
			DominoUtils.recycleQuietly(nn);
		}
	}
	
	// =========================================================================================================

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
	 * @param notesContext the notesContext to set
	 */
	public void setNotesContext(NotesContext notesContext) {
		this.notesContext = notesContext;
	}

	/**
	 * @param httpContext the httpContext to set
	 */
	public void setHttpContext(HttpContext httpContext) {
		this.httpContext = httpContext;
	}
}
