package com.github.lhervier.domino.oauth.library.server.ext;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


/**
 * Base class for a Spring Based Domino Servlet
 * @author Administrateur
 */
public abstract class DominoServlet extends HttpServlet {
	
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -1167165578644747248L;
	
	/**
	 * Spring ApplicationContext
	 */
	private ApplicationContext springContext;
	
	/**
	 * Constructeur
	 * ATTENTION: Le serveur d'application nous instancie deux fois :(
	 * @param configClass the configuration class
	 */
	public DominoServlet(Class<?> configurationClass) {
		super();
		
		// Spring se sert du classloader de la thread... qui n'est pas celui sert à charger la Servlet
		// Le 1er n'a accès qu'aux classes du plugin parent (celui sur lequel notre extension est branchée)
		// Alors que le second correspond à notre plugin.
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		ApplicationContext context;
		try {
			Thread.currentThread().setContextClassLoader(configurationClass.getClassLoader());
			context = new AnnotationConfigApplicationContext(DominoServletConfig.class, configurationClass);
		} finally {
			Thread.currentThread().setContextClassLoader(loader);
		}
		this.springContext = context;
	}
	
	/**
	 * Retourne le contexte Spring
	 */
	protected ApplicationContext getSpringContext() {
		return this.springContext;
	}
	
	/**
	 * @see javax.servlet.http.HttpServlet#service(HttpServletRequest, HttpServletResponse)
	 */
	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// On doit s'assurer que les sessions sont recyclées
		try {
			super.service(req, res);
		} finally {
			this.getSpringContext().getBean(NotesContext.class).threadShutdown();
		}
	}
}
