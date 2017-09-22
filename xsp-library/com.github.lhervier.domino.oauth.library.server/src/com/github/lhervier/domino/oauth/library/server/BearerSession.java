package com.github.lhervier.domino.oauth.library.server;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;

import lotus.domino.AdministrationProcess;
import lotus.domino.AgentContext;
import lotus.domino.Base;
import lotus.domino.ColorObject;
import lotus.domino.Database;
import lotus.domino.DateRange;
import lotus.domino.DateTime;
import lotus.domino.DbDirectory;
import lotus.domino.Directory;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.DxlExporter;
import lotus.domino.DxlImporter;
import lotus.domino.International;
import lotus.domino.Log;
import lotus.domino.Name;
import lotus.domino.Newsletter;
import lotus.domino.NotesCalendar;
import lotus.domino.NotesException;
import lotus.domino.PropertyBroker;
import lotus.domino.Registration;
import lotus.domino.RichTextParagraphStyle;
import lotus.domino.RichTextStyle;
import lotus.domino.Session;
import lotus.domino.Stream;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.github.lhervier.domino.oauth.common.utils.SystemUtils;
import com.github.lhervier.domino.oauth.library.server.services.SecretService;
import com.ibm.domino.napi.NException;
import com.ibm.domino.napi.c.NotesUtil;
import com.ibm.domino.napi.c.Os;
import com.ibm.domino.napi.c.xsp.XSPNative;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;

/**
 * Notes session opened as the user declared in the authorization http header
 * @author Lionel HERVIER
 */
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BearerSession implements Session {

	/**
	 * Logger
	 */
	private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(BearerSession.class);
	
	/**
	 * The http request
	 */
	@Autowired
	private HttpServletRequest request;
	
	/**
	 * Service to get the secrets
	 */
	@Autowired
	private SecretService secretSvc;
	
	/**
	 * The sign key
	 */
	@Value("${oauth2.server.core.signKey}")
	private String signKey;
	
	/**
	 * Delegated
	 */
	private Session delegated;

	/**
	 * The usernamelist
	 */
	private Long userNameList;
	
	/**
	 * The clientId
	 */
	private String clientId;
	
	/**
	 * The scopes
	 */
	private List<String> scopes;
	
	/**
	 * Bean initialization
	 */
	@PostConstruct
	public void init() {
		this.delegated = null;
		try {
			// Extract bearer token
			String auth = this.request.getHeader("Authorization");
			if( auth == null )
				return;
			if( !auth.startsWith("Bearer ") )
				return;
			String accessToken = auth.substring("Bearer ".length());
			
			// Parse the JWT
			JWSObject jwsObj = JWSObject.parse(accessToken);
			
			// Check sign key
			String kid = jwsObj.getHeader().getKeyID();
			if( !this.signKey.equals(kid) ) {
				LOG.error("kid incorrect in Bearer token");
				return;
			}
			
			// Check algorithm
			String alg = jwsObj.getHeader().getAlgorithm().getName();
			if( !"HS256".equals(alg) ) {
				LOG.error("alg incorrect in Bearer token");
				return;
			}
			
			// Check signature
			byte[] secret = this.secretSvc.getSignSecret(kid);
			JWSVerifier verifier = new MACVerifier(secret);
			if( !jwsObj.verify(verifier) ) {
				LOG.error("Bearer token verification failed");
				return;
			}
			
			// Check token is not expired
			JSONObject json = jwsObj.getPayload().toJSONObject();
			long expiration = json.getAsNumber("exp").longValue();
			if( expiration < SystemUtils.currentTimeSeconds() ) {
				LOG.error("Bearer token expired");
				return;
			}
			
			// Get clientId
			this.clientId = json.getAsString("aud");
			
			// Get the scopes
			JSONArray scopesArr = (JSONArray) json.get("scopes");
			this.scopes = new ArrayList<String>();
			for( Iterator<Object> it = scopesArr.listIterator(); it.hasNext(); ) {
				String scope = (String) it.next();
				this.scopes.add(scope);
			}
			
			// Get user name
			String userName = json.getAsString("sub");
			
			// Create session
			this.userNameList = NotesUtil.createUserNameList(userName);
			this.delegated = XSPNative.createXPageSession(userName, this.userNameList, false, false);
		} catch (NotesException e) {
			throw new RuntimeException(e);
		} catch (NException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		} catch (ParseException e) {
			return;
		}
	}
	
	/**
	 * Bean cleaup
	 */
	@PreDestroy
	public void cleanUp() {
		if( this.delegated == null )
			return;
		
		DominoUtils.recycleQuietly(this.delegated);
		this.delegated = null;
		
		try {
			Os.OSMemFree(this.userNameList);
		} catch (NException e) {
			throw new RuntimeException(e);
		}
		this.userNameList = null;
	}
	
	/**
	 * Return the clientId
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * Return the scopes
	 * @return the scopes
	 */
	public List<String> getScopes() {
		List<String> ret = new ArrayList<String>();
		ret.addAll(this.scopes);
		return ret;
	}

	/**
	 * Return true si the session is available
	 */
	public boolean isAvailable() {
		return this.delegated != null;
	}
	
	// ==========================================================================
	
	public AdministrationProcess createAdministrationProcess(String arg0)
			throws NotesException {
		return delegated.createAdministrationProcess(arg0);
	}

	public ColorObject createColorObject() throws NotesException {
		return delegated.createColorObject();
	}

	public DateRange createDateRange() throws NotesException {
		return delegated.createDateRange();
	}

	public DateRange createDateRange(Date arg0, Date arg1)
			throws NotesException {
		return delegated.createDateRange(arg0, arg1);
	}

	public DateRange createDateRange(DateTime arg0, DateTime arg1)
			throws NotesException {
		return delegated.createDateRange(arg0, arg1);
	}

	public DateTime createDateTime(Calendar arg0) throws NotesException {
		return delegated.createDateTime(arg0);
	}

	public DateTime createDateTime(Date arg0) throws NotesException {
		return delegated.createDateTime(arg0);
	}

	public DateTime createDateTime(String arg0) throws NotesException {
		return delegated.createDateTime(arg0);
	}

	public DxlExporter createDxlExporter() throws NotesException {
		return delegated.createDxlExporter();
	}

	public DxlImporter createDxlImporter() throws NotesException {
		return delegated.createDxlImporter();
	}

	public Log createLog(String arg0) throws NotesException {
		return delegated.createLog(arg0);
	}

	public Name createName(String arg0) throws NotesException {
		return delegated.createName(arg0);
	}

	public Name createName(String arg0, String arg1) throws NotesException {
		return delegated.createName(arg0, arg1);
	}

	public Newsletter createNewsletter(DocumentCollection arg0)
			throws NotesException {
		return delegated.createNewsletter(arg0);
	}

	public Registration createRegistration() throws NotesException {
		return delegated.createRegistration();
	}

	public RichTextParagraphStyle createRichTextParagraphStyle()
			throws NotesException {
		return delegated.createRichTextParagraphStyle();
	}

	public RichTextStyle createRichTextStyle() throws NotesException {
		return delegated.createRichTextStyle();
	}

	public Stream createStream() throws NotesException {
		return delegated.createStream();
	}

	@SuppressWarnings("unchecked")
	public Vector evaluate(String arg0) throws NotesException {
		return delegated.evaluate(arg0);
	}

	@SuppressWarnings("unchecked")
	public Vector evaluate(String arg0, Document arg1) throws NotesException {
		return delegated.evaluate(arg0, arg1);
	}

	@SuppressWarnings("unchecked")
	public Vector freeResourceSearch(DateTime arg0, DateTime arg1, String arg2,
			int arg3, int arg4) throws NotesException {
		return delegated.freeResourceSearch(arg0, arg1, arg2, arg3, arg4);
	}

	@SuppressWarnings("unchecked")
	public Vector freeResourceSearch(DateTime arg0, DateTime arg1, String arg2,
			int arg3, int arg4, String arg5, int arg6, String arg7,
			String arg8, int arg9) throws NotesException {
		return delegated.freeResourceSearch(arg0, arg1, arg2, arg3, arg4, arg5,
				arg6, arg7, arg8, arg9);
	}

	@SuppressWarnings("unchecked")
	public Vector freeTimeSearch(DateRange arg0, int arg1, Object arg2,
			boolean arg3) throws NotesException {
		return delegated.freeTimeSearch(arg0, arg1, arg2, arg3);
	}

	@SuppressWarnings("unchecked")
	public Vector getAddressBooks() throws NotesException {
		return delegated.getAddressBooks();
	}

	public AgentContext getAgentContext() throws NotesException {
		return delegated.getAgentContext();
	}

	public NotesCalendar getCalendar(Database arg0) throws NotesException {
		return delegated.getCalendar(arg0);
	}

	public String getCommonUserName() throws NotesException {
		return delegated.getCommonUserName();
	}

	public Object getCredentials() throws NotesException {
		return delegated.getCredentials();
	}

	public Database getCurrentDatabase() throws NotesException {
		return delegated.getCurrentDatabase();
	}

	public Database getDatabase(String arg0, String arg1) throws NotesException {
		return delegated.getDatabase(arg0, arg1);
	}

	public Database getDatabase(String arg0, String arg1, boolean arg2)
			throws NotesException {
		return delegated.getDatabase(arg0, arg1, arg2);
	}

	public DbDirectory getDbDirectory(String arg0) throws NotesException {
		return delegated.getDbDirectory(arg0);
	}

	public Directory getDirectory() throws NotesException {
		return delegated.getDirectory();
	}

	public Directory getDirectory(String arg0) throws NotesException {
		return delegated.getDirectory(arg0);
	}

	public String getEffectiveUserName() throws NotesException {
		return delegated.getEffectiveUserName();
	}

	public String getEnvironmentString(String arg0) throws NotesException {
		return delegated.getEnvironmentString(arg0);
	}

	public String getEnvironmentString(String arg0, boolean arg1)
			throws NotesException {
		return delegated.getEnvironmentString(arg0, arg1);
	}

	public Object getEnvironmentValue(String arg0) throws NotesException {
		return delegated.getEnvironmentValue(arg0);
	}

	public Object getEnvironmentValue(String arg0, boolean arg1)
			throws NotesException {
		return delegated.getEnvironmentValue(arg0, arg1);
	}

	public String getHttpURL() throws NotesException {
		return delegated.getHttpURL();
	}

	public International getInternational() throws NotesException {
		return delegated.getInternational();
	}

	public String getNotesVersion() throws NotesException {
		return delegated.getNotesVersion();
	}

	public String getOrgDirectoryPath() throws NotesException {
		return delegated.getOrgDirectoryPath();
	}

	public String getPlatform() throws NotesException {
		return delegated.getPlatform();
	}

	public PropertyBroker getPropertyBroker() throws NotesException {
		return delegated.getPropertyBroker();
	}

	public String getServerName() throws NotesException {
		return delegated.getServerName();
	}

	public String getSessionToken() throws NotesException {
		return delegated.getSessionToken();
	}

	public String getSessionToken(String arg0) throws NotesException {
		return delegated.getSessionToken(arg0);
	}

	public String getURL() throws NotesException {
		return delegated.getURL();
	}

	public Database getURLDatabase() throws NotesException {
		return delegated.getURLDatabase();
	}

	@SuppressWarnings("unchecked")
	public Vector getUserGroupNameList() throws NotesException {
		return delegated.getUserGroupNameList();
	}

	public String getUserName() throws NotesException {
		return delegated.getUserName();
	}

	@SuppressWarnings("unchecked")
	public Vector getUserNameList() throws NotesException {
		return delegated.getUserNameList();
	}

	public Name getUserNameObject() throws NotesException {
		return delegated.getUserNameObject();
	}

	public Document getUserPolicySettings(String arg0, String arg1, int arg2)
			throws NotesException {
		return delegated.getUserPolicySettings(arg0, arg1, arg2);
	}

	public Document getUserPolicySettings(String arg0, String arg1, int arg2,
			String arg3) throws NotesException {
		return delegated.getUserPolicySettings(arg0, arg1, arg2, arg3);
	}

	public String hashPassword(String arg0) throws NotesException {
		return delegated.hashPassword(arg0);
	}

	public boolean isConvertMIME() throws NotesException {
		return delegated.isConvertMIME();
	}

	public boolean isConvertMime() throws NotesException {
		return delegated.isConvertMime();
	}

	public boolean isOnServer() throws NotesException {
		return delegated.isOnServer();
	}

	public boolean isRestricted() throws NotesException {
		return delegated.isRestricted();
	}

	public boolean isTrackMillisecInJavaDates() throws NotesException {
		return delegated.isTrackMillisecInJavaDates();
	}

	public boolean isTrustedSession() throws NotesException {
		return delegated.isTrustedSession();
	}

	public boolean isValid() {
		return delegated.isValid();
	}

	public void recycle() throws NotesException {
		delegated.recycle();
	}

	@SuppressWarnings("unchecked")
	public void recycle(Vector arg0) throws NotesException {
		delegated.recycle(arg0);
	}

	public boolean resetUserPassword(String arg0, String arg1, String arg2)
			throws NotesException {
		return delegated.resetUserPassword(arg0, arg1, arg2);
	}

	public boolean resetUserPassword(String arg0, String arg1, String arg2,
			int arg3) throws NotesException {
		return delegated.resetUserPassword(arg0, arg1, arg2, arg3);
	}

	public Base resolve(String arg0) throws NotesException {
		return delegated.resolve(arg0);
	}

	public String sendConsoleCommand(String arg0, String arg1)
			throws NotesException {
		return delegated.sendConsoleCommand(arg0, arg1);
	}

	public void setAllowLoopBack(boolean arg0) throws NotesException {
		delegated.setAllowLoopBack(arg0);
	}

	public void setConvertMIME(boolean arg0) throws NotesException {
		delegated.setConvertMIME(arg0);
	}

	public void setConvertMime(boolean arg0) throws NotesException {
		delegated.setConvertMime(arg0);
	}

	public void setEnvironmentVar(String arg0, Object arg1)
			throws NotesException {
		delegated.setEnvironmentVar(arg0, arg1);
	}

	public void setEnvironmentVar(String arg0, Object arg1, boolean arg2)
			throws NotesException {
		delegated.setEnvironmentVar(arg0, arg1, arg2);
	}

	public void setTrackMillisecInJavaDates(boolean arg0) throws NotesException {
		delegated.setTrackMillisecInJavaDates(arg0);
	}

	public boolean verifyPassword(String arg0, String arg1)
			throws NotesException {
		return delegated.verifyPassword(arg0, arg1);
	}
}
