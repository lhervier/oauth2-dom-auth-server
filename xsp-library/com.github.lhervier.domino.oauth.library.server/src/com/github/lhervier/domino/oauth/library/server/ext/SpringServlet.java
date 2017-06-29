package com.github.lhervier.domino.oauth.library.server.ext;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;

import com.github.lhervier.domino.oauth.library.server.ext.ctx.HttpContext;
import com.github.lhervier.domino.oauth.library.server.ext.ctx.NotesContext;

/**
 * Base class for a Spring Based Domino Servlet
 * @author Administrateur
 */
public abstract class SpringServlet extends HttpServlet {
	
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -1167165578644747248L;
	
	/**
	 * @return the spring context
	 */
	public abstract ApplicationContext getSpringContext();
	
	/**
	 * @see javax.servlet.http.HttpServlet#service(HttpServletRequest, HttpServletResponse)
	 */
	@Override
	public final void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// Initialize and clean contexts before/after request processing
		try {
			this.getSpringContext().getBean(HttpContext.class).init(req, res);
			this.getSpringContext().getBean(NotesContext.class).init();
			super.service(req, res);
		} finally {
			this.getSpringContext().getBean(NotesContext.class).cleanUp();
			this.getSpringContext().getBean(HttpContext.class).cleanUp();
		}
	}
}
