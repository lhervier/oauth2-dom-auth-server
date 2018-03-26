package com.github.lhervier.domino.oauth.server.controller;

import org.springframework.web.servlet.ModelAndView;

import com.github.lhervier.domino.oauth.server.ex.ForbiddenException;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ex.WrongPathException;
import com.github.lhervier.domino.oauth.server.form.ApplicationForm;

public interface AppController {

	/**
	 * List applications
	 */
	public ModelAndView listApplications() throws NotAuthorizedException, ForbiddenException, WrongPathException;
	
	/**
	 * The screen to display the creation of a new application
	 */
	public ModelAndView createApplication() throws NotAuthorizedException, ForbiddenException, WrongPathException;
	
	/**
	 * Edits an application
	 */
	public ModelAndView editApplication(String appName) throws NotAuthorizedException, ForbiddenException, WrongPathException;
	
	/**
	 * Display the details of an application
	 */
	public ModelAndView viewApplication(String appName) throws NotAuthorizedException, ForbiddenException, WrongPathException;
	
	// ===============================================================
	
	/**
	 * Save an application
	 */
	public ModelAndView saveApplication(ApplicationForm form) throws NotAuthorizedException, ForbiddenException, WrongPathException;
	
	/**
	 * Removes an application
	 */
	public ModelAndView removeApplication(String name) throws NotAuthorizedException, ForbiddenException, WrongPathException;
	
}
