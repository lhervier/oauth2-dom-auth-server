package fr.asso.afer.oauth2.endpoints;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;
import fr.asso.afer.oauth2.Constants;
import fr.asso.afer.oauth2.app.AppBean;
import fr.asso.afer.oauth2.model.Application;
import fr.asso.afer.oauth2.utils.DominoUtils;
import fr.asso.afer.oauth2.utils.JSFUtils;

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
	 * @throws NotesException en cas de pb
	 * @throws IOException en cas de pb
	 */
	public void authorize() throws NotesException, IOException {
		Map<String, String> param = JSFUtils.getParam();
		String responseType = param.get("response_type");
		if( "code".equals(responseType) )
			this.authorizationCode();
		else
			throw new RuntimeException("Type de réponse inconnu '" + responseType + "'");
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
	 * @throws NotesException en cas de pb
	 * @throws IOException en cas de pb
	 */
	private void authorizationCode() throws NotesException, IOException {
		Map<String, String> param = JSFUtils.getParam();
		String clientId = param.get("client_id");
		String redirectUri = param.get("redirect_uri");
		String state = param.get("state");
		
		// Récupère l'application
		Application app = this.appBean.getApplicationFromClientId(clientId);
		if( app == null )
			throw new RuntimeException("Le client_id '" + clientId + "' ne correspond à aucune application...");
		
		// Vérifie que l'uri de redirection est bien dans la liste
		Set<String> redirectUris = new HashSet<String>();
		redirectUris.add(app.getRedirectUri());
		for( String uri : app.getRedirectUris() )
			redirectUris.add(uri);
		if( !redirectUris.contains(redirectUri) )
			throw new RuntimeException("Adresse de redirection invalide");
		
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
			
			authDoc.replaceItemValue("iss", "https://afer.asso.fr/oauth2/domino");
			authDoc.replaceItemValue("sub", this.session.getEffectiveUserName());
			authDoc.replaceItemValue("aud", app.getClientId());
			authDoc.replaceItemValue("exp", System.currentTimeMillis() + (10 * 60 * 1000));		// Expiration dans 10 minutes
			authDoc.replaceItemValue("iat", System.currentTimeMillis());
			authDoc.replaceItemValue("auth_time", System.currentTimeMillis());
			
			DominoUtils.computeAndSave(authDoc);
		} finally {
			DominoUtils.recycleQuietly(nn);
			DominoUtils.recycleQuietly(authDoc);
			DominoUtils.recycleQuietly(database);
		}
		
		// Construit l'url de redirection
		if( redirectUri.indexOf('?') == -1 )
			redirectUri += "?";
		else
			redirectUri += "&";
		redirectUri += "code=" + URLEncoder.encode(id, "UTF-8");
		if( state != null && state.length() != 0 )
			redirectUri += "&state=" + URLEncoder.encode(state, "UTF-8");
		
		// Redirige
		FacesContext ctx = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) ctx.getExternalContext().getResponse();
		response.sendRedirect(redirectUri);
	}
	
}
