package com.github.lhervier.domino.oauth.common.spring;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;

import com.github.lhervier.domino.oauth.common.ctx.SpringHttpContext;
import com.github.lhervier.domino.oauth.common.ctx.SpringNotesContext;

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
			this.getSpringContext().getBean(SpringHttpContext.class).init(req, res);
			this.getSpringContext().getBean(SpringNotesContext.class).init();
			super.service(req, res);
		} finally {
			this.getSpringContext().getBean(SpringNotesContext.class).cleanUp();
			this.getSpringContext().getBean(SpringHttpContext.class).cleanUp();
		}
	}
}
