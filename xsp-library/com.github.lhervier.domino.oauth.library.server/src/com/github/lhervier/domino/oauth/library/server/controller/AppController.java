package com.github.lhervier.domino.oauth.library.server.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import lotus.domino.NotesException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.github.lhervier.domino.oauth.library.server.aop.ann.Oauth2DbContext;
import com.github.lhervier.domino.oauth.library.server.aop.ann.Roles;
import com.github.lhervier.domino.oauth.library.server.model.Application;
import com.github.lhervier.domino.oauth.library.server.services.AppService;

@Controller
@RequestMapping(value = "/html")
public class AppController {

	/**
	 * The app service
	 */
	@Autowired
	private AppService appSvc;
	
	/**
	 * List applications
	 * @throws NotesException 
	 */
	@RequestMapping(value = "/listApplications")
	@Oauth2DbContext()
	@Roles(roles = {"AppsManager"})
	public ModelAndView listApplications() throws NotesException {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("apps", this.appSvc.getApplicationsNames());
		return new ModelAndView("applications", model);
	}
	
	// ====================================================================
	
	/**
	 * The screen to display the creation of a new application
	 */
	@RequestMapping(value = "/newApplication", method = RequestMethod.GET)
	@Oauth2DbContext
	@Roles(roles = "AppsManager")
	public ModelAndView createApplication() {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("app", this.appSvc.prepareApplication());
		model.put("edit", true);
		model.put("newApp", true);
		return new ModelAndView("application", model);
	}
	
	/**
	 * Display the details of an application
	 * @throws NotesException 
	 */
	@RequestMapping(value = "/viewApplication", method = RequestMethod.GET)
	@Oauth2DbContext
	@Roles(roles = {"AppsManager"})
	public ModelAndView viewApplication(@RequestParam(value = "name", required = true) String appName) throws NotesException {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("app", this.appSvc.getApplicationFromName(appName));
		model.put("edit", false);
		model.put("newApp", false);
		return new ModelAndView("application", model);
	}
	
	/**
	 * Edits an application
	 * @throws NotesException 
	 */
	@RequestMapping(value = "/editApplication", method = RequestMethod.GET)
	@Oauth2DbContext
	@Roles(roles = {"AppsManager"})
	public ModelAndView editApplication(@RequestParam(value = "name", required = true) String appName) throws NotesException {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("app", this.appSvc.getApplicationFromName(appName));
		model.put("edit", true);
		model.put("newApp", false);
		return new ModelAndView("application", model);
	}
	
	// ===============================================================
	
	/**
	 * An error object
	 */
	public static class AppError {
		private String name = null;
		private String readers = null;
		private String redirectUri = null;
		public boolean isError() {
			return (name != null) || (readers != null) || (redirectUri != null);
		}
		public String getName() {return name;}
		public void setName(String name) {this.name = name;}
		public String getReaders() {return readers;}
		public void setReaders(String readers) {this.readers = readers;}
		public String getRedirectUri() {return redirectUri;}
		public void setRedirectUri(String redirectUri) {this.redirectUri = redirectUri;}
	}
	
	/**
	 * Check an application object
	 * @return an error object
	 */
	private AppError checkApp(Application app) {
		AppError error = new AppError();
		if( StringUtils.isEmpty(app.getName()) )
			error.setName("name is mandatory");
		if( StringUtils.isEmpty(app.getReaders()) )
			error.setReaders("readers are mandatory");
		
		if( StringUtils.isEmpty(app.getRedirectUri()) ) {
			error.setRedirectUri("redirect_uri is mandatory");
		} else if( app.getRedirectUri().indexOf('#') != -1 ) {
			error.setRedirectUri("redirect_uri must not contain fragments ('#')");
		} else {
			try {
				new URI(app.getRedirectUri());
			} catch (URISyntaxException e) {
				error.setRedirectUri("redirect_uri must be a valid URI");
			}
		}
		return error;
	}
	
	/**
	 * Create a new application
	 * @throws NotesException 
	 */
	@RequestMapping(value = "/createApplication", method = RequestMethod.POST)
	@Oauth2DbContext
	@Roles(roles = "AppsManager")
	public ModelAndView newApplication(@ModelAttribute Application app) throws NotesException {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("app", app);
		model.put("newApp", true);
		
		// Check for errors
		AppError error = this.checkApp(app);
		if( error.isError() ) {
			model.put("error", error);
			model.put("edit", true);
			return new ModelAndView("application", model);
		}
		
		// No error, generate the secret
		String secret = this.appSvc.addApplication(app);
		model.put("secret", secret);
		
		// Display app in read only
		model.put("edit", false);
		return new ModelAndView("application", model);
	}
	
	/**
	 * Updates an application
	 * @throws NotesException 
	 */
	@RequestMapping(value = "/updateApplication", method = RequestMethod.POST)
	@Oauth2DbContext
	@Roles(roles = "AppsManager")
	public ModelAndView updateApplication(@ModelAttribute Application app) throws NotesException {
		Map<String, Object> model = new HashMap<String, Object>();
		AppError error = this.checkApp(app);
		if( error.isError() ) {
			model.put("app", app);
			model.put("edit", true);
			model.put("error", error);
			model.put("newApp", false);
			return new ModelAndView("application", model);
		}
		
		this.appSvc.updateApplication(app);
		return new ModelAndView("redirect:applications");
	}
	
	/**
	 * Removes an application
	 * @throws NotesException 
	 */
	@RequestMapping(value = "/deleteApplication", method = RequestMethod.GET)
	@Oauth2DbContext
	@Roles(roles = "AppsManager")
	public ModelAndView removeApplication(@RequestParam(value = "name", required = true) String name) throws NotesException {
		this.appSvc.removeApplication(name);
		return new ModelAndView("redirect:applications");
	}
	
}
