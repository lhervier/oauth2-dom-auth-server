package com.github.lhervier.domino.oauth.ext.openid;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.github.lhervier.domino.oauth.library.server.ext.DominoServlet;
import com.github.lhervier.domino.oauth.library.server.ext.NotesContext;

/**
 * Servlet for the "userInfo" openid endpoint
 * @author Lionel HERVIER
 */
public class UserInfoServlet extends DominoServlet {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -1167165578644747248L;
	
	/**
	 * Constructeur
	 */
	public UserInfoServlet() {
		super(UserInfoApplication.class);
	}

	/**
	 * Try to extract the value of document
	 * protected by ACL and reader field
	 * @param s the session to use
	 * @return the value of the field
	 * @throws NotesException
	 */
	private String getProtectedField(Session s) throws NotesException {
		Database db = null;
		View v = null;
		Document doc = null;
		try {
			try {
				db = s.getDatabase(null, "test.nsf");
			} catch(NotesException e) {
				return null;
			}
			if( !db.isOpen() )
				if( !db.open() )
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
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			// Annotated bean !
			MessageService msgSvc = this.getSpringContext().getBean(MessageService.class);
			msgSvc.message("Hello World !");
			
			// Annotated bean in the server plugin
			NotesContext provider = this.getSpringContext().getBean(NotesContext.class);
			Session userSession = provider.getUserSession();
			Session serverSession = provider.getServerSession();
			
			resp.getWriter().write("Header Authorization : " + req.getHeader("Authorization") + "\n");
			resp.getWriter().write("Effective Username : " + userSession.getEffectiveUserName() + "\n");
			resp.getWriter().write("URL : " + req.getPathInfo() + "\n");
			resp.getWriter().write("New session username : " + serverSession.getEffectiveUserName() + "\n");
			resp.getWriter().write("Field value in protected database (as user) : " + this.getProtectedField(userSession) + "\n");
			resp.getWriter().write("Field value in protected database (as server) : " + this.getProtectedField(serverSession) + "\n");
			
		} catch(NotesException e) {
			e.printStackTrace(System.err);
			throw new RuntimeException(e);
		}
	}
}
