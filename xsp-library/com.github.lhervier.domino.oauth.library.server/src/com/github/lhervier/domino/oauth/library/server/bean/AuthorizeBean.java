package com.github.lhervier.domino.oauth.library.server.bean;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;

import com.github.lhervier.domino.oauth.common.model.StateResponse;
import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.github.lhervier.domino.oauth.common.utils.JSFUtils;
import com.github.lhervier.domino.oauth.common.utils.QueryStringUtils;
import com.github.lhervier.domino.oauth.library.server.Constants;
import com.github.lhervier.domino.oauth.library.server.ex.AuthorizeException;
import com.github.lhervier.domino.oauth.library.server.ex.InvalidUriException;
import com.github.lhervier.domino.oauth.library.server.ex.authorize.InvalidRequestException;
import com.github.lhervier.domino.oauth.library.server.ex.authorize.ServerErrorException;
import com.github.lhervier.domino.oauth.library.server.ex.authorize.UnsupportedResponseTypeException;
import com.github.lhervier.domino.oauth.library.server.model.Application;
import com.github.lhervier.domino.oauth.library.server.model.AuthorizationCode;
import com.github.lhervier.domino.oauth.library.server.model.AuthorizeResponse;
import com.github.lhervier.domino.oauth.library.server.utils.Utils;

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
	 * La session
	 */
	private Session session;
	
	/**
	 * La session en tant que le signataire
	 */
	private Session sessionAsSigner;
	
	/**
	 * La bean pour accéder aux applications
	 */
	private AppBean appBean;
	
	/**
	 * La bean pour accéder au paramétrage
	 */
	private ParamsBean paramsBean;
	
	/**
	 * Constructeur
	 * @throws NotesException en cas de pb
	 */
	public AuthorizeBean() throws NotesException {
		this.session = JSFUtils.getSession();
		this.sessionAsSigner = JSFUtils.getSessionAsSigner();
		this.appBean = Utils.getAppBean();
		this.paramsBean = Utils.getParamsBean();
	}
	
	/**
	 * Génère le code autorization
	 * @throws IOException 
	 */
	public void authorize() throws IOException {
		Map<String, String> param = JSFUtils.getParam();
		StateResponse ret;
		String redirectUri = null;
		try {
			// Valide le redirectUri
			// => On ne doit pas (MUST NOT dans la RFC!) rediriger vers une uri invalide !
			redirectUri = param.get("redirect_uri");
			if( redirectUri == null )
				throw new InvalidUriException("No redirect_uri in query string.");
			try {
				new URI(redirectUri);
			} catch (URISyntaxException e) {
				throw new InvalidUriException("Invalid redirect_uri", e);
			}
			
			// Valide le clientId
			String clientId = param.get("client_id");
			if( clientId == null )
				throw new InvalidRequestException();
			
			// Valide le responseType
			String responseType = param.get("response_type");
			if( responseType == null )
				throw new InvalidRequestException();
			
			// Exécute le code grant
			if( "code".equals(responseType) )
				ret = this.authorizationCode(clientId, redirectUri);
			else
				throw new UnsupportedResponseTypeException();
		
		// Cas particulier (cf RFC) si l'uri de redirection est invalide
		} catch(InvalidUriException e) {
			e.printStackTrace(System.err);			// FIXME: Où envoyer ça ???
			JSFUtils.getRequestScope().put("error", e);
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
		ret.setState(param.get("state"));		// Eventuellement null
		
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
	 * @return l'url de redirection
	 * @throws AuthorizeException en cas de pb
	 * @throws InvalidUriException si l'uri est invalide
	 */
	private AuthorizeResponse authorizationCode(String clientId, String redirectUri) throws AuthorizeException, InvalidUriException {
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
		redirectUris.add(app.getRedirectUri());
		for( String uri : app.getRedirectUris() )
			redirectUris.add(uri);
		if( !redirectUris.contains(redirectUri) )
			throw new InvalidUriException("invalid redirect_uri");		// Cf RFC. On ne doit pas (MUST NOT) rediriger vers une uri invalide
		
		// Créé le document authorization
		String id = this.generateCode();
		Database db = null;
		Document authDoc = null;
		Name nn = null;
		try {
			db = DominoUtils.openDatabase(		// Si on ouvre cette dans le constructeur, parfois, elle est recyclée...
					this.sessionAsSigner, 
					JSFUtils.getDatabase().getFilePath()
			);
			nn = this.sessionAsSigner.createName(app.getName() + Utils.getParamsBean().getApplicationRoot());
			
			// Créé le code authorization
			AuthorizationCode authCode = new AuthorizationCode();
			authCode.setApplication(nn.toString());
			authCode.setId(id);
			authCode.setRedirectUri(redirectUri);
			authCode.setExpires(System.currentTimeMillis() + this.paramsBean.getAuthCodeLifetime());
			authCode.setIss(Constants.NAMESPACE);
			authCode.setSub(this.session.getEffectiveUserName());
			authCode.setAud(app.getClientId());
			authCode.setIat(System.currentTimeMillis());
			authCode.setAuthTime(System.currentTimeMillis());
			
			// On le persiste dans la base
			authDoc = db.createDocument();
			authDoc.replaceItemValue("Form", "AuthorizationCode");
			DominoUtils.fillDocument(authDoc, authCode);
			
			DominoUtils.computeAndSave(authDoc);
		} catch (NotesException e) {
			e.printStackTrace(System.err);
			throw new ServerErrorException();
		} finally {
			DominoUtils.recycleQuietly(nn);
			DominoUtils.recycleQuietly(authDoc);
			DominoUtils.recycleQuietly(db);
		}
		
		AuthorizeResponse ret = new AuthorizeResponse();
		ret.setCode(id);
		return ret;
	}
	
}
