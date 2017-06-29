package com.github.lhervier.domino.oauth.ext.openid;

import java.io.IOException;

import javax.servlet.ServletException;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.common.spring.ctx.HttpContext;
import com.github.lhervier.domino.oauth.common.spring.ctx.NotesContext;
import com.github.lhervier.domino.oauth.common.utils.DominoUtils;

@Component
public class UserInfoBean {

	/**
	 * The http context
	 */
	@Autowired
	private HttpContext httpContext;
	
	/**
	 * The NotesContext
	 */
	@Autowired
	private NotesContext notesContext;
	
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
			long curr = this.httpContext.getSession().getAttribute("test") == null ? 0L : (Long) this.httpContext.getSession().getAttribute("test");
			this.httpContext.getSession().setAttribute("test", curr + 1);
			this.httpContext.getResponse().getWriter().write("Incremental number from the httpSession : " + curr + "\n");
			
			// Annotated bean !
			this.httpContext.getResponse().getWriter().write("Method from annotated bean : " + this.messageService.getMessage("Hello World !") + "\n");
			
			// Standard servlet information: You can send a Authorization Bearer header here !
			this.httpContext.getResponse().getWriter().write("Authorization Header : " + this.httpContext.getRequest().getHeader("Authorization") + "\n");
			this.httpContext.getResponse().getWriter().write("URL : " + this.httpContext.getRequest().getPathInfo() + "\n");
			
			// Information from Domino
			this.httpContext.getResponse().getWriter().write("Current Username : " + this.notesContext.getUserSession().getEffectiveUserName() + "\n");
			this.httpContext.getResponse().getWriter().write("Server Username : " + this.notesContext.getServerSession().getEffectiveUserName() + "\n");
			
			if( this.notesContext.getUserDatabase() != null )
				this.httpContext.getResponse().getWriter().write("Current Database : " + this.notesContext.getUserDatabase().getFilePath() + "\n");
			if( this.notesContext.getServerDatabase() != null )
				this.httpContext.getResponse().getWriter().write("Server Database : " + this.notesContext.getServerDatabase().getFilePath() + "\n");
			
			this.httpContext.getResponse().getWriter().write("New httpSession username : " + this.notesContext.getServerSession().getEffectiveUserName() + "\n");
			this.httpContext.getResponse().getWriter().write("Field value in protected database (as user) : " + this.getProtectedField(this.notesContext.getUserSession()) + "\n");
			this.httpContext.getResponse().getWriter().write("Field value in protected database (as server) : " + this.getProtectedField(this.notesContext.getServerSession()) + "\n");
			
		} catch(NotesException e) {
			e.printStackTrace(System.err);
			throw new ServletException(e);
		}
	}
	
}
