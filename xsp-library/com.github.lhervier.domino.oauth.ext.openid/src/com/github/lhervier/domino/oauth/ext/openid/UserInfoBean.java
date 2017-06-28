package com.github.lhervier.domino.oauth.ext.openid;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.github.lhervier.domino.oauth.library.server.ext.DatabaseHolder;

@Component
public class UserInfoBean {

	/**
	 * The servlet request
	 */
	@Autowired
	private HttpServletRequest request;
	
	/**
	 * The servlet response
	 */
	@Autowired
	private HttpServletResponse response;
	
	/**
	 * The httpSession
	 */
	@Autowired
	private HttpSession httpSession;
	
	/**
	 * The user httpSession
	 */
	@Autowired
	private Session userSession;
	
	/**
	 * The server httpSession
	 */
	@Autowired
	private Session serverSession;
	
	/**
	 * The user database
	 */
	@Autowired
	private DatabaseHolder userDatabase;
	
	/**
	 * The server database
	 */
	@Autowired
	private DatabaseHolder serverDatabase;
	
	/**
	 * The message service
	 */
	@Autowired
	private MessageService messageService;
	
	/**
	 * Try to extract the value of document
	 * protected by ACL and reader field
	 * @param s the httpSession to use
	 * @return the value of the field
	 * @throws NotesException
	 */
	private String getProtectedField(Session s) throws NotesException {
		Database db = null;
		View v = null;
		Document doc = null;
		try {
			db = DominoUtils.openDatabase(s, "test.nsf");
			if( db == null )
				return null;
			v = db.getView("($All)");
			doc = v.getFirstDocument();
			if( doc == null || doc.getUniversalID().length() == 0 )
				return null;
			return doc.getItemValueString("ChampTest");
		} finally {
			DominoUtils.recycleQuietly(doc);
			DominoUtils.recycleQuietly(v);
			DominoUtils.recycleQuietly(db);
		}
	}
	
	/**
	 * Execution on a GET request
	 */
	public void goGet() throws ServletException, IOException {
		try {
			long curr = this.httpSession.getAttribute("test") == null ? 0L : (Long) this.httpSession.getAttribute("test");
			this.httpSession.setAttribute("test", curr + 1);
			this.response.getWriter().write("Incremental number from the httpSession : " + curr + "\n");
			
			// Annotated bean !
			this.response.getWriter().write("Method from annotated bean : " + this.messageService.getMessage("Hello World !") + "\n");
			
			// Standard servlet information: You can send a Authorization Bearer header here !
			this.response.getWriter().write("Authorization Header : " + this.request.getHeader("Authorization") + "\n");
			this.response.getWriter().write("URL : " + this.request.getPathInfo() + "\n");
			
			// Information from Domino
			this.response.getWriter().write("Current Username : " + this.userSession.getEffectiveUserName() + "\n");
			this.response.getWriter().write("Server Username : " + this.serverSession.getEffectiveUserName() + "\n");
			
			if( this.userDatabase.get() != null )
				this.response.getWriter().write("Current Database : " + this.userDatabase.get().getFilePath() + "\n");
			if( this.serverDatabase.get() != null )
				this.response.getWriter().write("Server Database : " + this.serverDatabase.get().getFilePath() + "\n");
			
			this.response.getWriter().write("New httpSession username : " + this.serverSession.getEffectiveUserName() + "\n");
			this.response.getWriter().write("Field value in protected database (as user) : " + this.getProtectedField(userSession) + "\n");
			this.response.getWriter().write("Field value in protected database (as server) : " + this.getProtectedField(serverSession) + "\n");
			
		} catch(NotesException e) {
			e.printStackTrace(System.err);
			throw new ServletException(e);
		}
	}
	
}
