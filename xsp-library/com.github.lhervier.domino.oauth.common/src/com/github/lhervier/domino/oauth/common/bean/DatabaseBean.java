package com.github.lhervier.domino.oauth.common.bean;

import java.util.Vector;

import lotus.domino.ACL;
import lotus.domino.Agent;
import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.Form;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;
import lotus.domino.Outline;
import lotus.domino.Replication;
import lotus.domino.Session;
import lotus.domino.View;

import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.github.lhervier.domino.oauth.common.utils.JSFUtils;

public class DatabaseBean implements Database {

	/**
	 * L'objet délégué
	 */
	private Database delegated;
	
	/**
	 * La session
	 */
	private Boolean asSigner;
	
	/**
	 * Le chemin vers la base
	 */
	private String filePath;
	
	/**
	 * Retourne la session
	 * @return la session
	 */
	private Session getSession() {
		if( this.asSigner != null && this.asSigner.booleanValue() )
			return JSFUtils.getSessionAsSigner();
		return JSFUtils.getSession();
	}
	
	/**
	 * Retourne la base
	 * @return la base
	 * @throws NotesException en cas de pb
	 */
	private synchronized Database getDelegate() throws NotesException {
		if( this.delegated != null && !DominoUtils.isRecycled(this.delegated) )
			return this.delegated;
		
		this.delegated = DominoUtils.openDatabase(
				this.getSession(), 
				this.filePath
		);
		if( this.delegated == null )
			throw new RuntimeException("Impossible d'ouvrir la base '" + this.filePath + "'");
		return this.delegated;
	}
	
	/**
	 * @param asSigner est ce qu'on doit ouvrir la base en tant que le signataire ?
	 */
	public void setAsSigner(Boolean asSigner) {
		this.asSigner = asSigner;
	}

	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	// =======================================================================================
	
	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 * @param arg6
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#FTDomainSearch(java.lang.String, int, int, int, int, int, java.lang.String)
	 */
	public Document FTDomainSearch(String arg0, int arg1, int arg2, int arg3,
			int arg4, int arg5, String arg6) throws NotesException {
		return this.getDelegate().FTDomainSearch(arg0, arg1, arg2, arg3, arg4, arg5,
				arg6);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#FTSearch(java.lang.String)
	 */
	public DocumentCollection FTSearch(String arg0) throws NotesException {
		return this.getDelegate().FTSearch(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#FTSearch(java.lang.String, int)
	 */
	public DocumentCollection FTSearch(String arg0, int arg1)
			throws NotesException {
		return this.getDelegate().FTSearch(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#FTSearch(java.lang.String, int, int, int)
	 */
	public DocumentCollection FTSearch(String arg0, int arg1, int arg2, int arg3)
			throws NotesException {
		return this.getDelegate().FTSearch(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#FTSearchRange(java.lang.String, int, int, int, int)
	 */
	public DocumentCollection FTSearchRange(String arg0, int arg1, int arg2,
			int arg3, int arg4) throws NotesException {
		return this.getDelegate().FTSearchRange(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#compact()
	 */
	public int compact() throws NotesException {
		return this.getDelegate().compact();
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#compactWithOptions(java.lang.String)
	 */
	public int compactWithOptions(String arg0) throws NotesException {
		return this.getDelegate().compactWithOptions(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#compactWithOptions(int)
	 */
	public int compactWithOptions(int arg0) throws NotesException {
		return this.getDelegate().compactWithOptions(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#compactWithOptions(int, java.lang.String)
	 */
	public int compactWithOptions(int arg0, String arg1) throws NotesException {
		return this.getDelegate().compactWithOptions(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#createCopy(java.lang.String, java.lang.String)
	 */
	public Database createCopy(String arg0, String arg1) throws NotesException {
		return this.getDelegate().createCopy(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#createCopy(java.lang.String, java.lang.String, int)
	 */
	public Database createCopy(String arg0, String arg1, int arg2)
			throws NotesException {
		return this.getDelegate().createCopy(arg0, arg1, arg2);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#createDocument()
	 */
	public Document createDocument() throws NotesException {
		return this.getDelegate().createDocument();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#createDocumentCollection()
	 */
	public DocumentCollection createDocumentCollection() throws NotesException {
		return this.getDelegate().createDocumentCollection();
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @throws NotesException
	 * @see lotus.domino.Database#createFTIndex(int, boolean)
	 */
	public void createFTIndex(int arg0, boolean arg1) throws NotesException {
		this.getDelegate().createFTIndex(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#createFromTemplate(java.lang.String, java.lang.String, boolean)
	 */
	public Database createFromTemplate(String arg0, String arg1, boolean arg2)
			throws NotesException {
		return this.getDelegate().createFromTemplate(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#createFromTemplate(java.lang.String, java.lang.String, boolean, int)
	 */
	public Database createFromTemplate(String arg0, String arg1, boolean arg2,
			int arg3) throws NotesException {
		return this.getDelegate().createFromTemplate(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#createNoteCollection(boolean)
	 */
	public NoteCollection createNoteCollection(boolean arg0)
			throws NotesException {
		return this.getDelegate().createNoteCollection(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#createOutline(java.lang.String)
	 */
	public Outline createOutline(String arg0) throws NotesException {
		return this.getDelegate().createOutline(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#createOutline(java.lang.String, boolean)
	 */
	public Outline createOutline(String arg0, boolean arg1)
			throws NotesException {
		return this.getDelegate().createOutline(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#createQueryView(java.lang.String, java.lang.String)
	 */
	public View createQueryView(String arg0, String arg1) throws NotesException {
		return this.getDelegate().createQueryView(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#createQueryView(java.lang.String, java.lang.String, lotus.domino.View)
	 */
	public View createQueryView(String arg0, String arg1, View arg2)
			throws NotesException {
		return this.getDelegate().createQueryView(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#createQueryView(java.lang.String, java.lang.String, lotus.domino.View, boolean)
	 */
	public View createQueryView(String arg0, String arg1, View arg2,
			boolean arg3) throws NotesException {
		return this.getDelegate().createQueryView(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#createReplica(java.lang.String, java.lang.String)
	 */
	public Database createReplica(String arg0, String arg1)
			throws NotesException {
		return this.getDelegate().createReplica(arg0, arg1);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#createView()
	 */
	public View createView() throws NotesException {
		return this.getDelegate().createView();
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#createView(java.lang.String)
	 */
	public View createView(String arg0) throws NotesException {
		return this.getDelegate().createView(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#createView(java.lang.String, java.lang.String)
	 */
	public View createView(String arg0, String arg1) throws NotesException {
		return this.getDelegate().createView(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#createView(java.lang.String, java.lang.String, lotus.domino.View)
	 */
	public View createView(String arg0, String arg1, View arg2)
			throws NotesException {
		return this.getDelegate().createView(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#createView(java.lang.String, java.lang.String, lotus.domino.View, boolean)
	 */
	public View createView(String arg0, String arg1, View arg2, boolean arg3)
			throws NotesException {
		return this.getDelegate().createView(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#enableFolder(java.lang.String)
	 */
	public void enableFolder(String arg0) throws NotesException {
		this.getDelegate().enableFolder(arg0);
	}

	/**
	 * @throws NotesException
	 * @see lotus.domino.Database#fixup()
	 */
	public void fixup() throws NotesException {
		this.getDelegate().fixup();
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#fixup(int)
	 */
	public void fixup(int arg0) throws NotesException {
		this.getDelegate().fixup(arg0);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getACL()
	 */
	public ACL getACL() throws NotesException {
		return this.getDelegate().getACL();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getACLActivityLog()
	 */
	@SuppressWarnings("unchecked")
	public Vector getACLActivityLog() throws NotesException {
		return this.getDelegate().getACLActivityLog();
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getAgent(java.lang.String)
	 */
	public Agent getAgent(String arg0) throws NotesException {
		return this.getDelegate().getAgent(arg0);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getAgents()
	 */
	@SuppressWarnings("unchecked")
	public Vector getAgents() throws NotesException {
		return this.getDelegate().getAgents();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getAllDocuments()
	 */
	public DocumentCollection getAllDocuments() throws NotesException {
		return this.getDelegate().getAllDocuments();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getAllReadDocuments()
	 */
	public DocumentCollection getAllReadDocuments() throws NotesException {
		return this.getDelegate().getAllReadDocuments();
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getAllReadDocuments(java.lang.String)
	 */
	public DocumentCollection getAllReadDocuments(String arg0)
			throws NotesException {
		return this.getDelegate().getAllReadDocuments(arg0);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getAllUnreadDocuments()
	 */
	public DocumentCollection getAllUnreadDocuments() throws NotesException {
		return this.getDelegate().getAllUnreadDocuments();
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getAllUnreadDocuments(java.lang.String)
	 */
	public DocumentCollection getAllUnreadDocuments(String arg0)
			throws NotesException {
		return this.getDelegate().getAllUnreadDocuments(arg0);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getCategories()
	 */
	public String getCategories() throws NotesException {
		return this.getDelegate().getCategories();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getCreated()
	 */
	public DateTime getCreated() throws NotesException {
		return this.getDelegate().getCreated();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getCurrentAccessLevel()
	 */
	public int getCurrentAccessLevel() throws NotesException {
		return this.getDelegate().getCurrentAccessLevel();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getDB2Schema()
	 */
	public String getDB2Schema() throws NotesException {
		return this.getDelegate().getDB2Schema();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getDesignTemplateName()
	 */
	public String getDesignTemplateName() throws NotesException {
		return this.getDelegate().getDesignTemplateName();
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getDocumentByID(java.lang.String)
	 */
	public Document getDocumentByID(String arg0) throws NotesException {
		return this.getDelegate().getDocumentByID(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getDocumentByUNID(java.lang.String)
	 */
	public Document getDocumentByUNID(String arg0) throws NotesException {
		return this.getDelegate().getDocumentByUNID(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getDocumentByURL(java.lang.String, boolean)
	 */
	public Document getDocumentByURL(String arg0, boolean arg1)
			throws NotesException {
		return this.getDelegate().getDocumentByURL(arg0, arg1);
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
	 * @see lotus.domino.Database#getDocumentByURL(java.lang.String, boolean, boolean, boolean, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	public Document getDocumentByURL(String arg0, boolean arg1, boolean arg2,
			boolean arg3, String arg4, String arg5, String arg6, String arg7,
			String arg8, boolean arg9) throws NotesException {
		return this.getDelegate().getDocumentByURL(arg0, arg1, arg2, arg3, arg4, arg5,
				arg6, arg7, arg8, arg9);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getFTIndexFrequency()
	 */
	public int getFTIndexFrequency() throws NotesException {
		return this.getDelegate().getFTIndexFrequency();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getFileFormat()
	 */
	public int getFileFormat() throws NotesException {
		return this.getDelegate().getFileFormat();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getFileName()
	 */
	public String getFileName() throws NotesException {
		return this.getDelegate().getFileName();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getFilePath()
	 */
	public String getFilePath() throws NotesException {
		return this.getDelegate().getFilePath();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getFolderReferencesEnabled()
	 */
	public boolean getFolderReferencesEnabled() throws NotesException {
		return this.getDelegate().getFolderReferencesEnabled();
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getForm(java.lang.String)
	 */
	public Form getForm(String arg0) throws NotesException {
		return this.getDelegate().getForm(arg0);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getForms()
	 */
	@SuppressWarnings("unchecked")
	public Vector getForms() throws NotesException {
		return this.getDelegate().getForms();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getHttpURL()
	 */
	public String getHttpURL() throws NotesException {
		return this.getDelegate().getHttpURL();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getLastFTIndexed()
	 */
	public DateTime getLastFTIndexed() throws NotesException {
		return this.getDelegate().getLastFTIndexed();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getLastFixup()
	 */
	public DateTime getLastFixup() throws NotesException {
		return this.getDelegate().getLastFixup();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getLastModified()
	 */
	public DateTime getLastModified() throws NotesException {
		return this.getDelegate().getLastModified();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getLimitRevisions()
	 */
	public double getLimitRevisions() throws NotesException {
		return this.getDelegate().getLimitRevisions();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getLimitUpdatedBy()
	 */
	public double getLimitUpdatedBy() throws NotesException {
		return this.getDelegate().getLimitUpdatedBy();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getListInDbCatalog()
	 */
	public boolean getListInDbCatalog() throws NotesException {
		return this.getDelegate().getListInDbCatalog();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getManagers()
	 */
	@SuppressWarnings("unchecked")
	public Vector getManagers() throws NotesException {
		return this.getDelegate().getManagers();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getMaxSize()
	 */
	public long getMaxSize() throws NotesException {
		return this.getDelegate().getMaxSize();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getModifiedDocuments()
	 */
	public DocumentCollection getModifiedDocuments() throws NotesException {
		return this.getDelegate().getModifiedDocuments();
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getModifiedDocuments(lotus.domino.DateTime)
	 */
	public DocumentCollection getModifiedDocuments(DateTime arg0)
			throws NotesException {
		return this.getDelegate().getModifiedDocuments(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getModifiedDocuments(lotus.domino.DateTime, int)
	 */
	public DocumentCollection getModifiedDocuments(DateTime arg0, int arg1)
			throws NotesException {
		return this.getDelegate().getModifiedDocuments(arg0, arg1);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getNotesURL()
	 */
	public String getNotesURL() throws NotesException {
		return this.getDelegate().getNotesURL();
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getOption(int)
	 */
	public boolean getOption(int arg0) throws NotesException {
		return this.getDelegate().getOption(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getOutline(java.lang.String)
	 */
	public Outline getOutline(String arg0) throws NotesException {
		return this.getDelegate().getOutline(arg0);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getParent()
	 */
	public Session getParent() throws NotesException {
		return this.getDelegate().getParent();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getPercentUsed()
	 */
	public double getPercentUsed() throws NotesException {
		return this.getDelegate().getPercentUsed();
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getProfileDocCollection(java.lang.String)
	 */
	public DocumentCollection getProfileDocCollection(String arg0)
			throws NotesException {
		return this.getDelegate().getProfileDocCollection(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getProfileDocument(java.lang.String, java.lang.String)
	 */
	public Document getProfileDocument(String arg0, String arg1)
			throws NotesException {
		return this.getDelegate().getProfileDocument(arg0, arg1);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getReplicaID()
	 */
	public String getReplicaID() throws NotesException {
		return this.getDelegate().getReplicaID();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getReplicationInfo()
	 */
	public Replication getReplicationInfo() throws NotesException {
		return this.getDelegate().getReplicationInfo();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getServer()
	 */
	public String getServer() throws NotesException {
		return this.getDelegate().getServer();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getSize()
	 */
	public double getSize() throws NotesException {
		return this.getDelegate().getSize();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getSizeQuota()
	 */
	public int getSizeQuota() throws NotesException {
		return this.getDelegate().getSizeQuota();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getSizeWarning()
	 */
	public long getSizeWarning() throws NotesException {
		return this.getDelegate().getSizeWarning();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getTemplateName()
	 */
	public String getTemplateName() throws NotesException {
		return this.getDelegate().getTemplateName();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getTitle()
	 */
	public String getTitle() throws NotesException {
		return this.getDelegate().getTitle();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getType()
	 */
	public int getType() throws NotesException {
		return this.getDelegate().getType();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getURL()
	 */
	public String getURL() throws NotesException {
		return this.getDelegate().getURL();
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getURLHeaderInfo(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public String getURLHeaderInfo(String arg0, String arg1, String arg2,
			String arg3, String arg4, String arg5) throws NotesException {
		return this.getDelegate().getURLHeaderInfo(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getUndeleteExpireTime()
	 */
	public int getUndeleteExpireTime() throws NotesException {
		return this.getDelegate().getUndeleteExpireTime();
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getView(java.lang.String)
	 */
	public View getView(String arg0) throws NotesException {
		return this.getDelegate().getView(arg0);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#getViews()
	 */
	@SuppressWarnings("unchecked")
	public Vector getViews() throws NotesException {
		return this.getDelegate().getViews();
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @throws NotesException
	 * @see lotus.domino.Database#grantAccess(java.lang.String, int)
	 */
	public void grantAccess(String arg0, int arg1) throws NotesException {
		this.getDelegate().grantAccess(arg0, arg1);
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#isAllowOpenSoftDeleted()
	 */
	public boolean isAllowOpenSoftDeleted() throws NotesException {
		return this.getDelegate().isAllowOpenSoftDeleted();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#isClusterReplication()
	 */
	public boolean isClusterReplication() throws NotesException {
		return this.getDelegate().isClusterReplication();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#isConfigurationDirectory()
	 */
	public boolean isConfigurationDirectory() throws NotesException {
		return this.getDelegate().isConfigurationDirectory();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#isCurrentAccessPublicReader()
	 */
	public boolean isCurrentAccessPublicReader() throws NotesException {
		return this.getDelegate().isCurrentAccessPublicReader();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#isCurrentAccessPublicWriter()
	 */
	public boolean isCurrentAccessPublicWriter() throws NotesException {
		return this.getDelegate().isCurrentAccessPublicWriter();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#isDB2()
	 */
	public boolean isDB2() throws NotesException {
		return this.getDelegate().isDB2();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#isDelayUpdates()
	 */
	public boolean isDelayUpdates() throws NotesException {
		return this.getDelegate().isDelayUpdates();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#isDesignLockingEnabled()
	 */
	public boolean isDesignLockingEnabled() throws NotesException {
		return this.getDelegate().isDesignLockingEnabled();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#isDirectoryCatalog()
	 */
	public boolean isDirectoryCatalog() throws NotesException {
		return this.getDelegate().isDirectoryCatalog();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#isDocumentLockingEnabled()
	 */
	public boolean isDocumentLockingEnabled() throws NotesException {
		return this.getDelegate().isDocumentLockingEnabled();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#isFTIndexed()
	 */
	public boolean isFTIndexed() throws NotesException {
		return this.getDelegate().isFTIndexed();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#isInMultiDbIndexing()
	 */
	public boolean isInMultiDbIndexing() throws NotesException {
		return this.getDelegate().isInMultiDbIndexing();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#isInService()
	 */
	public boolean isInService() throws NotesException {
		return this.getDelegate().isInService();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#isLink()
	 */
	public boolean isLink() throws NotesException {
		return this.getDelegate().isLink();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#isMultiDbSearch()
	 */
	public boolean isMultiDbSearch() throws NotesException {
		return this.getDelegate().isMultiDbSearch();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#isOpen()
	 */
	public boolean isOpen() throws NotesException {
		return this.getDelegate().isOpen();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#isPendingDelete()
	 */
	public boolean isPendingDelete() throws NotesException {
		return this.getDelegate().isPendingDelete();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#isPrivateAddressBook()
	 */
	public boolean isPrivateAddressBook() throws NotesException {
		return this.getDelegate().isPrivateAddressBook();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#isPublicAddressBook()
	 */
	public boolean isPublicAddressBook() throws NotesException {
		return this.getDelegate().isPublicAddressBook();
	}

	/**
	 * @throws NotesException
	 * @see lotus.domino.Database#markForDelete()
	 */
	public void markForDelete() throws NotesException {
		this.getDelegate().markForDelete();
	}

	/**
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#open()
	 */
	public boolean open() throws NotesException {
		return this.getDelegate().open();
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#openByReplicaID(java.lang.String, java.lang.String)
	 */
	public boolean openByReplicaID(String arg0, String arg1)
			throws NotesException {
		return this.getDelegate().openByReplicaID(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#openIfModified(java.lang.String, java.lang.String, lotus.domino.DateTime)
	 */
	public boolean openIfModified(String arg0, String arg1, DateTime arg2)
			throws NotesException {
		return this.getDelegate().openIfModified(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#openWithFailover(java.lang.String, java.lang.String)
	 */
	public boolean openWithFailover(String arg0, String arg1)
			throws NotesException {
		return this.getDelegate().openWithFailover(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#queryAccess(java.lang.String)
	 */
	public int queryAccess(String arg0) throws NotesException {
		return this.getDelegate().queryAccess(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#queryAccessPrivileges(java.lang.String)
	 */
	public int queryAccessPrivileges(String arg0) throws NotesException {
		return this.getDelegate().queryAccessPrivileges(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#queryAccessRoles(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Vector queryAccessRoles(String arg0) throws NotesException {
		return this.getDelegate().queryAccessRoles(arg0);
	}

	/**
	 * @throws NotesException
	 * @see lotus.domino.Base#recycle()
	 */
	public void recycle() throws NotesException {
		this.getDelegate().recycle();
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Base#recycle(java.util.Vector)
	 */
	@SuppressWarnings("unchecked")
	public void recycle(Vector arg0) throws NotesException {
		this.getDelegate().recycle(arg0);
	}

	/**
	 * @throws NotesException
	 * @see lotus.domino.Database#remove()
	 */
	public void remove() throws NotesException {
		this.getDelegate().remove();
	}

	/**
	 * @throws NotesException
	 * @see lotus.domino.Database#removeFTIndex()
	 */
	public void removeFTIndex() throws NotesException {
		this.getDelegate().removeFTIndex();
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#replicate(java.lang.String)
	 */
	public boolean replicate(String arg0) throws NotesException {
		return this.getDelegate().replicate(arg0);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#revokeAccess(java.lang.String)
	 */
	public void revokeAccess(String arg0) throws NotesException {
		this.getDelegate().revokeAccess(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#search(java.lang.String)
	 */
	public DocumentCollection search(String arg0) throws NotesException {
		return this.getDelegate().search(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#search(java.lang.String, lotus.domino.DateTime)
	 */
	public DocumentCollection search(String arg0, DateTime arg1)
			throws NotesException {
		return this.getDelegate().search(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 * @throws NotesException
	 * @see lotus.domino.Database#search(java.lang.String, lotus.domino.DateTime, int)
	 */
	public DocumentCollection search(String arg0, DateTime arg1, int arg2)
			throws NotesException {
		return this.getDelegate().search(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#setAllowOpenSoftDeleted(boolean)
	 */
	public void setAllowOpenSoftDeleted(boolean arg0) throws NotesException {
		this.getDelegate().setAllowOpenSoftDeleted(arg0);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#setCategories(java.lang.String)
	 */
	public void setCategories(String arg0) throws NotesException {
		this.getDelegate().setCategories(arg0);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#setDelayUpdates(boolean)
	 */
	public void setDelayUpdates(boolean arg0) throws NotesException {
		this.getDelegate().setDelayUpdates(arg0);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#setDesignLockingEnabled(boolean)
	 */
	public void setDesignLockingEnabled(boolean arg0) throws NotesException {
		this.getDelegate().setDesignLockingEnabled(arg0);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#setDocumentLockingEnabled(boolean)
	 */
	public void setDocumentLockingEnabled(boolean arg0) throws NotesException {
		this.getDelegate().setDocumentLockingEnabled(arg0);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#setFTIndexFrequency(int)
	 */
	public void setFTIndexFrequency(int arg0) throws NotesException {
		this.getDelegate().setFTIndexFrequency(arg0);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#setFolderReferencesEnabled(boolean)
	 */
	public void setFolderReferencesEnabled(boolean arg0) throws NotesException {
		this.getDelegate().setFolderReferencesEnabled(arg0);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#setInMultiDbIndexing(boolean)
	 */
	public void setInMultiDbIndexing(boolean arg0) throws NotesException {
		this.getDelegate().setInMultiDbIndexing(arg0);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#setInService(boolean)
	 */
	public void setInService(boolean arg0) throws NotesException {
		this.getDelegate().setInService(arg0);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#setLimitRevisions(double)
	 */
	public void setLimitRevisions(double arg0) throws NotesException {
		this.getDelegate().setLimitRevisions(arg0);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#setLimitUpdatedBy(double)
	 */
	public void setLimitUpdatedBy(double arg0) throws NotesException {
		this.getDelegate().setLimitUpdatedBy(arg0);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#setListInDbCatalog(boolean)
	 */
	public void setListInDbCatalog(boolean arg0) throws NotesException {
		this.getDelegate().setListInDbCatalog(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @throws NotesException
	 * @see lotus.domino.Database#setOption(int, boolean)
	 */
	public void setOption(int arg0, boolean arg1) throws NotesException {
		this.getDelegate().setOption(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#setSizeQuota(int)
	 */
	public void setSizeQuota(int arg0) throws NotesException {
		this.getDelegate().setSizeQuota(arg0);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#setSizeWarning(int)
	 */
	public void setSizeWarning(int arg0) throws NotesException {
		this.getDelegate().setSizeWarning(arg0);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#setTitle(java.lang.String)
	 */
	public void setTitle(String arg0) throws NotesException {
		this.getDelegate().setTitle(arg0);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#setUndeleteExpireTime(int)
	 */
	public void setUndeleteExpireTime(int arg0) throws NotesException {
		this.getDelegate().setUndeleteExpireTime(arg0);
	}

	/**
	 * @throws NotesException
	 * @see lotus.domino.Database#sign()
	 */
	public void sign() throws NotesException {
		this.getDelegate().sign();
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#sign(int)
	 */
	public void sign(int arg0) throws NotesException {
		this.getDelegate().sign(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @throws NotesException
	 * @see lotus.domino.Database#sign(int, boolean)
	 */
	public void sign(int arg0, boolean arg1) throws NotesException {
		this.getDelegate().sign(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @throws NotesException
	 * @see lotus.domino.Database#sign(int, boolean, java.lang.String)
	 */
	public void sign(int arg0, boolean arg1, String arg2) throws NotesException {
		this.getDelegate().sign(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @throws NotesException
	 * @see lotus.domino.Database#sign(int, boolean, java.lang.String, boolean)
	 */
	public void sign(int arg0, boolean arg1, String arg2, boolean arg3)
			throws NotesException {
		this.getDelegate().sign(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param arg0
	 * @throws NotesException
	 * @see lotus.domino.Database#updateFTIndex(boolean)
	 */
	public void updateFTIndex(boolean arg0) throws NotesException {
		this.getDelegate().updateFTIndex(arg0);
	}
	
}
