package fr.asso.afer.oauth2.endpoints;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;
import fr.asso.afer.oauth2.Constants;
import fr.asso.afer.oauth2.app.AppBean;
import fr.asso.afer.oauth2.ex.AuthorizeException;
import fr.asso.afer.oauth2.ex.InvalidUriException;
import fr.asso.afer.oauth2.ex.authorize.AccessDeniedException;
import fr.asso.afer.oauth2.ex.authorize.InvalidRequestException;
import fr.asso.afer.oauth2.ex.authorize.ServerErrorException;
import fr.asso.afer.oauth2.ex.authorize.UnsupportedResponseTypeException;
import fr.asso.afer.oauth2.model.Application;
import fr.asso.afer.oauth2.model.AuthorizeResponse;
import fr.asso.afer.oauth2.model.StateResponse;
import fr.asso.afer.oauth2.utils.DominoUtils;
import fr.asso.afer.oauth2.utils.JSFUtils;
import fr.asso.afer.oauth2.utils.QueryStringUtils;

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
	 * La bean pour accéder aux applications
	 */
	private AppBean appBean;
	
	/**
	 * Constructeur
	 * @throws NotesException en cas de pb
	 */
	public AuthorizeBean() throws NotesException {
		this.session = JSFUtils.getSession();
		this.appBean = JSFUtils.getAppBean();
	}
	
	/**
	 * Génère le code autorization
	 * @param response la réponse http
	 * @throws IOException 
	 */
	public void authorize(HttpServletResponse response) throws IOException {
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
		response.sendRedirect(QueryStringUtils.addBeanToQueryString(redirectUri, ret));
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
			throw new AccessDeniedException();
		
		// Vérifie que l'uri de redirection est bien dans la liste
		Set<String> redirectUris = new HashSet<String>();
		redirectUris.add(app.getRedirectUri());
		for( String uri : app.getRedirectUris() )
			redirectUris.add(uri);
		if( !redirectUris.contains(redirectUri) )
			throw new InvalidUriException("invalid redirect_uri");		// Cf RFC. On ne doit pas (MUST NOT) rediriger vers une uri invalide
		
		// Créé le document authorization
		String id = this.generateCode();
		Document authDoc = null;
		Database database = null;
		Name nn = null;
		try {
			database = JSFUtils.getDatabase();
			nn = this.session.createName(app.getName() + Constants.SUFFIX_APP);
			authDoc = database.createDocument();
			authDoc.replaceItemValue("Form", "AuthorizationCode");
			authDoc.replaceItemValue("Application", nn.toString());
			authDoc.replaceItemValue("ID", id);
			authDoc.replaceItemValue("RedirectUri", redirectUri);
			
			authDoc.replaceItemValue("iss", Constants.NAMESPACE);
			authDoc.replaceItemValue("sub", this.session.getEffectiveUserName());
			authDoc.replaceItemValue("aud", app.getClientId());
			authDoc.replaceItemValue("authDate", System.currentTimeMillis());
			authDoc.replaceItemValue("iat", System.currentTimeMillis());
			authDoc.replaceItemValue("auth_time", System.currentTimeMillis());
			
			DominoUtils.computeAndSave(authDoc);
		} catch (NotesException e) {
			throw new ServerErrorException();
		} finally {
			DominoUtils.recycleQuietly(nn);
			DominoUtils.recycleQuietly(authDoc);
			DominoUtils.recycleQuietly(database);
		}
		
		AuthorizeResponse ret = new AuthorizeResponse();
		ret.setCode(id);
		return ret;
	}
	
}
