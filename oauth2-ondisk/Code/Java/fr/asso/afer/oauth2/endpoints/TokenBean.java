package fr.asso.afer.oauth2.endpoints;

import java.util.Map;

import fr.asso.afer.oauth2.app.AppBean;
import fr.asso.afer.oauth2.model.Application;
import fr.asso.afer.oauth2.utils.DominoUtils;
import fr.asso.afer.oauth2.utils.JSFUtils;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

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
	 * Constructeur
	 * @throws NotesException en cas de pb
	 */
	public TokenBean() throws NotesException {
		this.session = JSFUtils.getSession();
		this.database = JSFUtils.getDatabase();
		this.appBean = JSFUtils.getAppBean();
		this.v = DominoUtils.getView(this.database, VIEW_AUTHCODES);
	}
	
	/**
	 * Gestion du token
	 * @throws NotesException en cas de pb
	 */
	public void token() throws NotesException {
		Map<String, String> param = JSFUtils.getParam();
		String grantType = param.get("grant_type");
		
		if( "authorization_code".equals(grantType) )
			this.authorizationCode();
		else
			throw new RuntimeException("Type de grant '" + grantType + "' inconnu...");
	}
	
	/**
	 * Génération d'un token à partir d'un code autorisation
	 * @throws NotesException en cas de pb
	 */
	public void authorizationCode() throws NotesException {
		Map<String, String> param = JSFUtils.getParam();
		String code = param.get("code");
		String redirectUri = param.get("redirect_uri");
		String clientId = param.get("client_id");
		
		Name nn = null;
		Document doc = null;
		try {
			nn = this.session.createName(this.session.getEffectiveUserName());
			
			// Récupère l'application
			Application app = this.appBean.getApplicationFromName(nn.getCommon());
			if( app == null )
				throw new RuntimeException("Application '" + nn.getCommon() + "' inconnue...");
			
			// Récupère le document correspondant au code authorization
			doc = this.v.getDocumentByKey(code, true);
			if( doc == null )
				throw new RuntimeException("Code autorisation incorrect");
			
			// Vérifie qu'il n'est pas expiré
			long expired = (long) doc.getItemValueDouble("exp");
			if( expired < System.currentTimeMillis() )
				throw new RuntimeException("Code autorisation incorrect");
			
			// Vérifie que le clientId est le bon
			if( !clientId.equals(doc.getItemValueString("aud")) )
				throw new RuntimeException("Code autorisation incorrect");
			
			// Vérifie que l'uri de redirection est la même
			if( !redirectUri.equals(doc.getItemValueString("RedirectUri")) )
				throw new RuntimeException("Code autorisation incorrect");
			
			// Génère le token
			
			
		} finally {
			DominoUtils.recycleQuietly(nn);
			DominoUtils.recycleQuietly(doc);
		}
	}
}
