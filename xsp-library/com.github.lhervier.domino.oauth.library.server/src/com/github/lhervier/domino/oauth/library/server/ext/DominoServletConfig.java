package com.github.lhervier.domino.oauth.library.server.ext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lotus.domino.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class DominoServletConfig {

	/**
	 * The notes context
	 */
	@Autowired
	private NotesContext notesContext;
	
	/**
	 * The http context
	 */
	@Autowired
	private HttpContext httpContext;
	
	/**
	 * Return the user session
	 * WARN : Method name = bean name for autowire
	 */
	@Bean
	public Session userSession() {
		return new WrappedUserSession();
	}
	
	/**
	 * Return the server session
	 * WARN : Method name = bean name for autowire
	 */
	@Bean
	public Session serverSession() {
		return new WrappedServerSession(this.notesContext);
	}
	
	/**
	 * Return the current database.
	 * Published in a DatabaseHolder to manage the null case.
	 * WARN : Method name = bean name for autowire
	 */
	@Bean
	public DatabaseHolder userDatabase() {
		return new DatabaseHolder(new WrappedUserDatabase());
	}
	
	/**
	 * Return the current database
	 * Published in a DatabaseHolder to manage the null case.
	 * WARN : Method name = bean name for autowire
	 */
	@Bean
	public DatabaseHolder serverDatabase() {
		return new DatabaseHolder(new WrappedServerDatabase(this.notesContext));
	}
	
	/**
	 * Return the current request
	 */
	@Bean
	public HttpServletRequest servletRequest() {
		return new WrappedHttpServletRequest(this.httpContext);
	}
	
	/**
	 * Return the current response
	 */
	@Bean
	public HttpServletResponse servletResponse() {
		return new WrappedHttpServletResponse(this.httpContext);
	}
	
	/**
	 * Return the current session
	 */
	@Bean
	public HttpSession httpSession() {
		return new WrappedHttpSession(this.httpContext);
	}
}
