package com.github.lhervier.domino.oauth.library.server.bean;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lotus.domino.Document;
import lotus.domino.Name;
import lotus.domino.NotesException;

import org.apache.commons.lang.StringUtils;

import com.github.lhervier.domino.oauth.common.HttpContext;
import com.github.lhervier.domino.oauth.common.NotesContext;
import com.github.lhervier.domino.oauth.common.model.StateResponse;
import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.github.lhervier.domino.oauth.common.utils.HttpUtils;
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
	 * The notes context
	 */
	private NotesContext notesContext;
	
	/**
	 * The http context
	 */
	private HttpContext httpContext;
	
	/**
	 * Génère le code autorization
	 * @throws InvalidUriException
	 */
	public void authorize() throws InvalidUriException {
		StateResponse ret;
		String redirectUri = null;
		try {
			// Le responseType est obligatoire
			String responseType = this.httpContext.getRequest().getParameter("response_type");
			if( StringUtils.isEmpty(responseType) )
				throw new InvalidRequestException();
			
			// Authorization Code Grant
			// ===========================
			if( "code".equals(responseType) ) {
				// Le clientId est obligatoire
				String clientId = this.httpContext.getRequest().getParameter("client_id");
				if( StringUtils.isEmpty(clientId) )
					throw new InvalidRequestException("No client_id in query string.");
				
				// Le redirectUri est obligatoire
				// FIXME: En fonction du response_type, le redirectUri peut être facultatif
				// Voir https://tools.ietf.org/html/rfc6749#section-4.1.1
				redirectUri = this.httpContext.getRequest().getParameter("redirect_uri");
				if( StringUtils.isEmpty(redirectUri) )
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
				if( StringUtils.isEmpty(this.httpContext.getRequest().getParameter("scope")) )
					scopes = new ArrayList<String>();
				else
					scopes = Arrays.asList(StringUtils.split(this.httpContext.getRequest().getParameter("scope"), " "));
				ret = this.authorizationCode(
						clientId, 
						redirectUri, 
						scopes
				);
			} else
				throw new UnsupportedResponseTypeException();
		
		// Erreur pendant l'autorisation
		} catch(AuthorizeException e) {
			e.printStackTrace(System.err);			// FIXME: Où envoyer ça ???
			ret = e.getError();
		
		// Autre erreur
		} catch(Throwable e) {
			e.printStackTrace(System.err);			// FIXME: Où envoyer ça ???
			throw new RuntimeException(e);
		}
		
		// Pas de réponse => Pas de redirection
		if( ret == null )
			return;
		
		// Ajoute le state
		ret.setState(this.httpContext.getRequest().getParameter("state"));		// Eventuellement null
		
		// Redirige
		HttpUtils.sendRedirect(this.httpContext.getResponse(), QueryStringUtils.addBeanToQueryString(redirectUri, ret));
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
			nn = this.notesContext.getServerDatabase().getParent().createName(app.getName() + this.paramsBean.getApplicationRoot());
			
			// Créé le code authorization
			AuthorizationCode authCode = new AuthorizationCode();
			authCode.setId(id);
			authCode.setApplication(nn.toString());
			authCode.setClientId(app.getClientId());
			authCode.setRedirectUri(redirectUri);
			authCode.setExpires(SystemUtils.currentTimeSeconds() + this.paramsBean.getAuthCodeLifetime());
			
			// Défini le scope. 
			authCode.setScopes(scopes);
			
			// Appel les plugins pour générer les plugins, et mettre à jour les scopes autorisés
			final List<String> grantedScopes = new ArrayList<String>();
			JsonObject contexts = new JsonObject();
			
			List<IOAuthExtension> exts = Utils.getExtensions();
			for( IOAuthExtension ext : exts ) {
				JsonObject jsonConf = this.paramsBean.getPluginConf(ext.getId());
				JsonObject context = ext.authorize(
						this.httpContext,
						this.notesContext,
						jsonConf,
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
			authDoc = this.notesContext.getServerDatabase().createDocument();
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
