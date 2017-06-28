package com.github.lhervier.domino.oauth.library.server.ext;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

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

public abstract class BaseWrappedSession implements Session {

	/**
	 * Retourne la session
	 */
	public abstract Session getSession();

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#createAdministrationProcess(java.lang.String)
	 */
	public AdministrationProcess createAdministrationProcess(String arg0)
			throws NotesException {
		return this.getSession().createAdministrationProcess(arg0);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#createColorObject()
	 */
	public ColorObject createColorObject() throws NotesException {
		return this.getSession().createColorObject();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#createDateRange()
	 */
	public DateRange createDateRange() throws NotesException {
		return this.getSession().createDateRange();
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#createDateRange(java.util.Date, java.util.Date)
	 */
	public DateRange createDateRange(Date arg0, Date arg1)
			throws NotesException {
		return this.getSession().createDateRange(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#createDateRange(lotus.domino.DateTime, lotus.domino.DateTime)
	 */
	public DateRange createDateRange(DateTime arg0, DateTime arg1)
			throws NotesException {
		return this.getSession().createDateRange(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#createDateTime(java.util.Calendar)
	 */
	public DateTime createDateTime(Calendar arg0) throws NotesException {
		return this.getSession().createDateTime(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#createDateTime(java.util.Date)
	 */
	public DateTime createDateTime(Date arg0) throws NotesException {
		return this.getSession().createDateTime(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#createDateTime(java.lang.String)
	 */
	public DateTime createDateTime(String arg0) throws NotesException {
		return this.getSession().createDateTime(arg0);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#createDxlExporter()
	 */
	public DxlExporter createDxlExporter() throws NotesException {
		return this.getSession().createDxlExporter();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#createDxlImporter()
	 */
	public DxlImporter createDxlImporter() throws NotesException {
		return this.getSession().createDxlImporter();
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#createLog(java.lang.String)
	 */
	public Log createLog(String arg0) throws NotesException {
		return this.getSession().createLog(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#createName(java.lang.String)
	 */
	public Name createName(String arg0) throws NotesException {
		return this.getSession().createName(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#createName(java.lang.String, java.lang.String)
	 */
	public Name createName(String arg0, String arg1) throws NotesException {
		return this.getSession().createName(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#createNewsletter(lotus.domino.DocumentCollection)
	 */
	public Newsletter createNewsletter(DocumentCollection arg0)
			throws NotesException {
		return this.getSession().createNewsletter(arg0);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#createRegistration()
	 */
	public Registration createRegistration() throws NotesException {
		return this.getSession().createRegistration();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#createRichTextParagraphStyle()
	 */
	public RichTextParagraphStyle createRichTextParagraphStyle()
			throws NotesException {
		return this.getSession().createRichTextParagraphStyle();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#createRichTextStyle()
	 */
	public RichTextStyle createRichTextStyle() throws NotesException {
		return this.getSession().createRichTextStyle();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#createStream()
	 */
	public Stream createStream() throws NotesException {
		return this.getSession().createStream();
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#evaluate(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Vector evaluate(String arg0) throws NotesException {
		return this.getSession().evaluate(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#evaluate(java.lang.String, lotus.domino.Document)
	 */
	@SuppressWarnings("unchecked")
	public Vector evaluate(String arg0, Document arg1) throws NotesException {
		return this.getSession().evaluate(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#freeResourceSearch(lotus.domino.DateTime, lotus.domino.DateTime, java.lang.String, int, int)
	 */
	@SuppressWarnings("unchecked")
	public Vector freeResourceSearch(DateTime arg0, DateTime arg1, String arg2,
			int arg3, int arg4) throws NotesException {
		return this.getSession().freeResourceSearch(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 * @param arg6
	 * @param arg7
	 * @param arg8
	 * @param arg9
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#freeResourceSearch(lotus.domino.DateTime, lotus.domino.DateTime, java.lang.String, int, int, java.lang.String, int, java.lang.String, java.lang.String, int)
	 */
	@SuppressWarnings("unchecked")
	public Vector freeResourceSearch(DateTime arg0, DateTime arg1, String arg2,
			int arg3, int arg4, String arg5, int arg6, String arg7,
			String arg8, int arg9) throws NotesException {
		return this.getSession().freeResourceSearch(arg0, arg1, arg2, arg3, arg4, arg5,
				arg6, arg7, arg8, arg9);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#freeTimeSearch(lotus.domino.DateRange, int, java.lang.Object, boolean)
	 */
	@SuppressWarnings("unchecked")
	public Vector freeTimeSearch(DateRange arg0, int arg1, Object arg2,
			boolean arg3) throws NotesException {
		return this.getSession().freeTimeSearch(arg0, arg1, arg2, arg3);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getAddressBooks()
	 */
	@SuppressWarnings("unchecked")
	public Vector getAddressBooks() throws NotesException {
		return this.getSession().getAddressBooks();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getAgentContext()
	 */
	public AgentContext getAgentContext() throws NotesException {
		return this.getSession().getAgentContext();
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getCalendar(lotus.domino.Database)
	 */
	public NotesCalendar getCalendar(Database arg0) throws NotesException {
		return this.getSession().getCalendar(arg0);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getCommonUserName()
	 */
	public String getCommonUserName() throws NotesException {
		return this.getSession().getCommonUserName();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getCredentials()
	 */
	public Object getCredentials() throws NotesException {
		return this.getSession().getCredentials();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getCurrentDatabase()
	 */
	public Database getCurrentDatabase() throws NotesException {
		return this.getSession().getCurrentDatabase();
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getDatabase(java.lang.String, java.lang.String)
	 */
	public Database getDatabase(String arg0, String arg1) throws NotesException {
		return this.getSession().getDatabase(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getDatabase(java.lang.String, java.lang.String, boolean)
	 */
	public Database getDatabase(String arg0, String arg1, boolean arg2)
			throws NotesException {
		return this.getSession().getDatabase(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getDbDirectory(java.lang.String)
	 */
	public DbDirectory getDbDirectory(String arg0) throws NotesException {
		return this.getSession().getDbDirectory(arg0);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getDirectory()
	 */
	public Directory getDirectory() throws NotesException {
		return this.getSession().getDirectory();
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getDirectory(java.lang.String)
	 */
	public Directory getDirectory(String arg0) throws NotesException {
		return this.getSession().getDirectory(arg0);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getEffectiveUserName()
	 */
	public String getEffectiveUserName() throws NotesException {
		return this.getSession().getEffectiveUserName();
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getEnvironmentString(java.lang.String)
	 */
	public String getEnvironmentString(String arg0) throws NotesException {
		return this.getSession().getEnvironmentString(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getEnvironmentString(java.lang.String, boolean)
	 */
	public String getEnvironmentString(String arg0, boolean arg1)
			throws NotesException {
		return this.getSession().getEnvironmentString(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getEnvironmentValue(java.lang.String)
	 */
	public Object getEnvironmentValue(String arg0) throws NotesException {
		return this.getSession().getEnvironmentValue(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getEnvironmentValue(java.lang.String, boolean)
	 */
	public Object getEnvironmentValue(String arg0, boolean arg1)
			throws NotesException {
		return this.getSession().getEnvironmentValue(arg0, arg1);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getHttpURL()
	 */
	public String getHttpURL() throws NotesException {
		return this.getSession().getHttpURL();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getInternational()
	 */
	public International getInternational() throws NotesException {
		return this.getSession().getInternational();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getNotesVersion()
	 */
	public String getNotesVersion() throws NotesException {
		return this.getSession().getNotesVersion();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getOrgDirectoryPath()
	 */
	public String getOrgDirectoryPath() throws NotesException {
		return this.getSession().getOrgDirectoryPath();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getPlatform()
	 */
	public String getPlatform() throws NotesException {
		return this.getSession().getPlatform();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getPropertyBroker()
	 */
	public PropertyBroker getPropertyBroker() throws NotesException {
		return this.getSession().getPropertyBroker();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getServerName()
	 */
	public String getServerName() throws NotesException {
		return this.getSession().getServerName();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getSessionToken()
	 */
	public String getSessionToken() throws NotesException {
		return this.getSession().getSessionToken();
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getSessionToken(java.lang.String)
	 */
	public String getSessionToken(String arg0) throws NotesException {
		return this.getSession().getSessionToken(arg0);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getURL()
	 */
	public String getURL() throws NotesException {
		return this.getSession().getURL();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getURLDatabase()
	 */
	public Database getURLDatabase() throws NotesException {
		return this.getSession().getURLDatabase();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getUserGroupNameList()
	 */
	@SuppressWarnings("unchecked")
	public Vector getUserGroupNameList() throws NotesException {
		return this.getSession().getUserGroupNameList();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getUserName()
	 */
	public String getUserName() throws NotesException {
		return this.getSession().getUserName();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getUserNameList()
	 */
	@SuppressWarnings("unchecked")
	public Vector getUserNameList() throws NotesException {
		return this.getSession().getUserNameList();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getUserNameObject()
	 */
	public Name getUserNameObject() throws NotesException {
		return this.getSession().getUserNameObject();
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getUserPolicySettings(java.lang.String, java.lang.String, int)
	 */
	public Document getUserPolicySettings(String arg0, String arg1, int arg2)
			throws NotesException {
		return this.getSession().getUserPolicySettings(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#getUserPolicySettings(java.lang.String, java.lang.String, int, java.lang.String)
	 */
	public Document getUserPolicySettings(String arg0, String arg1, int arg2,
			String arg3) throws NotesException {
		return this.getSession().getUserPolicySettings(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#hashPassword(java.lang.String)
	 */
	public String hashPassword(String arg0) throws NotesException {
		return this.getSession().hashPassword(arg0);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#isConvertMIME()
	 */
	public boolean isConvertMIME() throws NotesException {
		return this.getSession().isConvertMIME();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#isConvertMime()
	 */
	public boolean isConvertMime() throws NotesException {
		return this.getSession().isConvertMime();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#isOnServer()
	 */
	public boolean isOnServer() throws NotesException {
		return this.getSession().isOnServer();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#isRestricted()
	 */
	public boolean isRestricted() throws NotesException {
		return this.getSession().isRestricted();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#isTrackMillisecInJavaDates()
	 */
	public boolean isTrackMillisecInJavaDates() throws NotesException {
		return this.getSession().isTrackMillisecInJavaDates();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#isTrustedSession()
	 */
	public boolean isTrustedSession() throws NotesException {
		return this.getSession().isTrustedSession();
	}

	/**
	 * @return
	 * @see lotus.domino.Session#isValid()
	 */
	public boolean isValid() {
		return this.getSession().isValid();
	}

	/**
	 * @throws NotesException
	 * @see lotus.domino.Base#recycle()
	 */
	public void recycle() throws NotesException {
		this.getSession().recycle();
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Base#recycle(java.util.Vector)
	 */
	@SuppressWarnings("unchecked")
	public void recycle(Vector arg0) throws NotesException {
		this.getSession().recycle(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#resetUserPassword(java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean resetUserPassword(String arg0, String arg1, String arg2)
			throws NotesException {
		return this.getSession().resetUserPassword(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#resetUserPassword(java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public boolean resetUserPassword(String arg0, String arg1, String arg2,
			int arg3) throws NotesException {
		return this.getSession().resetUserPassword(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#resolve(java.lang.String)
	 */
	public Base resolve(String arg0) throws NotesException {
		return this.getSession().resolve(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#sendConsoleCommand(java.lang.String, java.lang.String)
	 */
	public String sendConsoleCommand(String arg0, String arg1)
			throws NotesException {
		return this.getSession().sendConsoleCommand(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Session#setAllowLoopBack(boolean)
	 */
	public void setAllowLoopBack(boolean arg0) throws NotesException {
		this.getSession().setAllowLoopBack(arg0);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Session#setConvertMIME(boolean)
	 */
	public void setConvertMIME(boolean arg0) throws NotesException {
		this.getSession().setConvertMIME(arg0);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Session#setConvertMime(boolean)
	 */
	public void setConvertMime(boolean arg0) throws NotesException {
		this.getSession().setConvertMime(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @throws NotesException
	 * @see lotus.domino.Session#setEnvironmentVar(java.lang.String, java.lang.Object)
	 */
	public void setEnvironmentVar(String arg0, Object arg1)
			throws NotesException {
		this.getSession().setEnvironmentVar(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @throws NotesException
	 * @see lotus.domino.Session#setEnvironmentVar(java.lang.String, java.lang.Object, boolean)
	 */
	public void setEnvironmentVar(String arg0, Object arg1, boolean arg2)
			throws NotesException {
		this.getSession().setEnvironmentVar(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Session#setTrackMillisecInJavaDates(boolean)
	 */
	public void setTrackMillisecInJavaDates(boolean arg0) throws NotesException {
		this.getSession().setTrackMillisecInJavaDates(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Session#verifyPassword(java.lang.String, java.lang.String)
	 */
	public boolean verifyPassword(String arg0, String arg1)
			throws NotesException {
		return this.getSession().verifyPassword(arg0, arg1);
	};
}
