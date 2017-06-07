package com.github.lhervier.domino.oauth.library.server.bean;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;

import org.apache.commons.lang.StringUtils;

import com.github.lhervier.domino.oauth.common.model.StateResponse;
import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.github.lhervier.domino.oauth.common.utils.JSFUtils;
import com.github.lhervier.domino.oauth.common.utils.QueryStringUtils;
import com.github.lhervier.domino.oauth.common.utils.SystemUtils;
import com.github.lhervier.domino.oauth.library.server.ex.AuthorizeException;
import com.github.lhervier.domino.oauth.library.server.ex.InvalidUriException;
import com.github.lhervier.domino.oauth.library.server.ex.authorize.InvalidRequestException;
import com.github.lhervier.domino.oauth.library.server.ex.authorize.ServerErrorException;
import com.github.lhervier.domino.oauth.library.server.ex.authorize.UnsupportedResponseTypeException;
import com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.library.server.ext.IScopeGranter;
import com.github.lhervier.domino.oauth.library.server.model.Application;
import com.github.lhervier.domino.oauth.library.server.model.AuthorizationCode;
import com.github.lhervier.domino.oauth.library.server.model.AuthorizeResponse;
import com.github.lhervier.domino.oauth.library.server.utils.Utils;
import com.google.gson.JsonObject;

/**
 * Bean pour gérer le endpoint "authorize"
 * @author Lionel HERVIER
 */
public class AuthorizeBean {

	/**
	 * Notre générateur de nombres aléatoires
	 */
	private static final SecureRandom RANDOM = new SecureRandom();
	
	/**
	 * La bean pour accéder aux applications
	 */
	private AppBean appBean;
	
	/**
	 * La bean pour accéder au paramétrage
	 */
	private ParamsBean paramsBean;
	
	/**
	 * Les paramètres dans la requête
	 */
	private Map<String, String> param;
	
	/**
	 * La databaseAsSigner courante
	 */
	private Database databaseAsSigner;
	
	/**
	 * La session courante
	 */
	private Session session;
	
	/**
	 * Le requestScope
	 */
	private Map<String, Object> requestScope;
	
	/**
	 * Génère le code autorization
	 * @throws IOException 
	 */
	public void authorize() throws IOException {
		StateResponse ret;
		String redirectUri = null;
		try {
			// Le responseType est obligatoire
			String responseType = this.param.get("response_type");
			if( responseType == null )
				throw new InvalidRequestException();
			
			// Authorization Code Grant
			// ===========================
			if( "code".equals(responseType) ) {
				// Le clientId est obligatoire
				String clientId = this.param.get("client_id");
				if( clientId == null )
					throw new InvalidRequestException("No client_id in query string.");
				
				// Le redirectUri est obligatoire
				// FIXME: En fonction du response_type, le redirectUri peut être facultatif
				// Voir https://tools.ietf.org/html/rfc6749#section-4.1.1
				redirectUri = this.param.get("redirect_uri");
				if( redirectUri == null )
					throw new InvalidUriException("No redirect_uri in query string.");
				try {
					URI uri = new URI(redirectUri);
					if( !uri.isAbsolute() )
						throw new InvalidUriException("Invalid redirect_uri");
				} catch (URISyntaxException e) {
					throw new InvalidUriException("Invalid redirect_uri", e);
				}
				
				// Exécution
				List<String> scopes;
				if( this.param.get("scope") == null )
					scopes = new ArrayList<String>();
				else
					scopes = Arrays.asList(StringUtils.split(this.param.get("scope"), " "));
				ret = this.authorizationCode(
						clientId, 
						redirectUri, 
						scopes
				);
			} else
				throw new UnsupportedResponseTypeException();
		
		// Cas particulier (cf RFC) si l'uri de redirection est invalide
		} catch(InvalidUriException e) {
			e.printStackTrace(System.err);			// FIXME: Où envoyer ça ???
			this.requestScope.put("error", e);
			ret = null;
		
		// Erreur pendant l'autorisation
		} catch(AuthorizeException e) {
			e.printStackTrace(System.err);			// FIXME: Où envoyer ça ???
			ret = e.getError();
		}
		
		// Pas de réponse => Pas de redirection
		if( ret == null )
			return;
		
		// Ajoute le state
		ret.setState(this.param.get("state"));		// Eventuellement null
		
		// Redirige
		JSFUtils.sendRedirect(QueryStringUtils.addBeanToQueryString(redirectUri, ret));
	}
	
	// ===========================================================================================================
	
	/**
	 * Génère un identifiant pour les codes autorization
	 * @return l'identifiant
	 */
	private String generateCode() {
		// see https://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
		return new BigInteger(260, RANDOM).toString(32);
	}
	
	/**
	 * Traitement d'une demande d'authorisation pour un code autorization.
	 * @param clientId l'id du client
	 * @param redirectUri l'uri de redirection
	 * @param scopes les scopes FIXME: Il ne sont pas utilisés
	 * @return l'url de redirection
	 * @throws AuthorizeException en cas de pb
	 * @throws InvalidUriException si l'uri est invalide
	 */
	private AuthorizeResponse authorizationCode(
			String clientId, 
			String redirectUri,
			List<String> scopes) throws AuthorizeException, InvalidUriException {
		// Récupère l'application
		Application app;
		try {
			app = this.appBean.getApplicationFromClientId(clientId);
		} catch (NotesException e) {
			throw new ServerErrorException(e);
		}
		if( app == null )
			throw new ServerErrorException();
		
		// Vérifie que l'uri de redirection est bien dans la liste
		Set<String> redirectUris = new HashSet<String>();
		redirectUris.add(app.getRedirectUri().toString());
		for( URI uri : app.getRedirectUris() )
			redirectUris.add(uri.toString());
		if( !redirectUris.contains(redirectUri) )
			throw new InvalidUriException("invalid redirect_uri");		// Cf RFC. On ne doit pas (MUST NOT) rediriger vers une uri invalide
		
		// Créé le document authorization
		String id = this.generateCode();
		Document authDoc = null;
		Name nn = null;
		try {
			nn = this.databaseAsSigner.getParent().createName(app.getName() + this.paramsBean.getApplicationRoot());
			
			// Créé le code authorization
			AuthorizationCode authCode = new AuthorizationCode();
			authCode.setApplication(nn.toString());
			authCode.setId(id);
			authCode.setRedirectUri(redirectUri);
			authCode.setExpires(SystemUtils.currentTimeSeconds() + this.paramsBean.getAuthCodeLifetime());
			authCode.setUser(this.session.getEffectiveUserName());
			authCode.setClientId(app.getClientId());
			
			// Défini le scope. 
			authCode.setScopes(scopes);
			
			// Appel les plugins pour générer les plugins, et mettre à jour les scopes autorisés
			final List<String> grantedScopes = new ArrayList<String>();
			JsonObject contexts = new JsonObject();
			
			List<IOAuthExtension> exts = Utils.getExtensions();
			for( IOAuthExtension ext : exts ) {
				JsonObject context = ext.authorize(
						new IScopeGranter() {
							@Override
							public void grant(String scope) {
								grantedScopes.add(scope);
							}
						}, 
						clientId, 
						scopes
				);
				if( context != null )
					contexts.add(ext.getId(), context);
			}
			
			authCode.setGrantedScopes(grantedScopes);
			authCode.setContexts(contexts);
			
			// On le persiste dans la base
			authDoc = this.databaseAsSigner.createDocument();
			authDoc.replaceItemValue("Form", "AuthorizationCode");
			DominoUtils.fillDocument(authDoc, authCode);
			
			DominoUtils.computeAndSave(authDoc);
		} catch (NotesException e) {
			e.printStackTrace(System.err);
			throw new ServerErrorException();
		} finally {
			DominoUtils.recycleQuietly(nn);
			DominoUtils.recycleQuietly(authDoc);
		}
		
		AuthorizeResponse ret = new AuthorizeResponse();
		ret.setCode(id);
		return ret;
	}
	
	// ==============================================================================================

	/**
	 * @param appBean the appBean to set
	 */
	public void setAppBean(AppBean appBean) {
		this.appBean = appBean;
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
	 * @param databaseAsSigner the databaseAsSigner to set
	 */
	public void setDatabaseAsSigner(Database database) {
		this.databaseAsSigner = database;
	}

	/**
	 * @param requestScope the requestScope to set
	 */
	public void setRequestScope(Map<String, Object> requestScope) {
		this.requestScope = requestScope;
	}

	/**
	 * @param session the session to set
	 */
	public void setSession(Session session) {
		this.session = session;
	}
	
}
