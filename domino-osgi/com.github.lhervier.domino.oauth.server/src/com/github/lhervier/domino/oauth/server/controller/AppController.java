package com.github.lhervier.domino.oauth.server.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import lotus.domino.NotesException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.github.lhervier.domino.oauth.server.aop.ann.ctx.Oauth2DbContext;
import com.github.lhervier.domino.oauth.server.aop.ann.security.Roles;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.services.AppService;

@Controller
@RequestMapping(value = "/html")
@Roles(roles = {"AppsManager"})
@Oauth2DbContext()
public class AppController {

	/**
	 * Redirect URIs attribute
	 */
	private static final String ATTR_REDIRECT_URIS = "REDIRECT_URIS";
	
	/**
	 * The app service
	 */
	@Autowired
	private AppService appSvc;
	
	/**
	 * The http session
	 */
	@Autowired
	private HttpSession session;
	
	/**
	 * List applications
	 * @throws NotesException 
	 */
	@RequestMapping(value = "/listApplications")
	public ModelAndView listApplications() throws NotesException {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("apps", this.appSvc.getApplicationsNames());
		return new ModelAndView("applications", model);
	}
	
	/**
	 * The screen to display the creation of a new application
	 */
	@RequestMapping(value = "/newApplication", method = RequestMethod.GET)
	public ModelAndView createApplication() {
		Map<String, Object> model = new HashMap<String, Object>();
		Application app = this.appSvc.prepareApplication();
		model.put("app", app);
		model.put("edit", true);
		model.put("newApp", true);
		this.session.setAttribute(ATTR_REDIRECT_URIS, app.getRedirectUris());
		return new ModelAndView("application", model);
	}
	
	/**
	 * Edits an application
	 * @throws NotesException 
	 */
	@RequestMapping(value = "/editApplication", method = RequestMethod.GET)
	public ModelAndView editApplication(@RequestParam(value = "name", required = true) String appName) throws NotesException {
		Map<String, Object> model = new HashMap<String, Object>();
		Application app = this.appSvc.getApplicationFromName(appName);
		model.put("app", app);
		model.put("edit", true);
		model.put("newApp", false);
		this.session.setAttribute(ATTR_REDIRECT_URIS, app.getRedirectUris());
		return new ModelAndView("application", model);
	}
	
	/**
	 * Display the details of an application
	 * @throws NotesException 
	 */
	@RequestMapping(value = "/viewApplication", method = RequestMethod.GET)
	public ModelAndView viewApplication(@RequestParam(value = "name", required = true) String appName) throws NotesException {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("app", this.appSvc.getApplicationFromName(appName));
		model.put("edit", false);
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
		private String newRedirectUri = null;
		public boolean isError() {
			return (name != null) || (readers != null) || (redirectUri != null) || (newRedirectUri != null);
		}
		public String getName() {return name;}
		public void setName(String name) {this.name = name;}
		public String getReaders() {return readers;}
		public void setReaders(String readers) {this.readers = readers;}
		public String getRedirectUri() {return redirectUri;}
		public void setRedirectUri(String redirectUri) {this.redirectUri = redirectUri;}
		public String getNewRedirectUri() { return newRedirectUri; }
		public void setNewRedirectUri(String newRedirectUri) { this.newRedirectUri = newRedirectUri; }
	}
	
	/**
	 * Check a redirect Uri
	 */
	private String checkRedirectUri(String redirectUri) {
		if( StringUtils.isEmpty(redirectUri) )
			return "redirect_uri is mandatory";
		
		if( redirectUri.indexOf('#') != -1 )
			return "redirect_uri must not contain fragments ('#')";
		
		try {
			URI uri = new URI(redirectUri);
			if( !uri.isAbsolute() )
				return "redirect_uri must be an absolute URI";
		} catch (URISyntaxException e) {
			return "redirect_uri must be a valid URI";
		}
		
		return null;
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
		
		error.setRedirectUri(this.checkRedirectUri(app.getRedirectUri()));
		
		return error;
	}
	
	/**
	 * Save an application
	 * @throws NotesException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/saveApplication", method = RequestMethod.POST)
	public ModelAndView saveApplication(
			@ModelAttribute Application app,
			@RequestParam String newRedirectUri,
			@RequestParam String action) throws NotesException {
		Map<String, Object> model = new HashMap<String, Object>();
		app.setRedirectUris((List<String>) this.session.getAttribute(ATTR_REDIRECT_URIS));
		model.put("app", app);
		
		// New app ?
		boolean newApp = this.appSvc.getApplicationFromClientId(app.getClientId()) == null;
		model.put("newApp", newApp);
		
		// Just want to add a redirectUri
		if( "addRedirectUri".equals(action) ) {
			model.put("edit", true);
			String err = this.checkRedirectUri(newRedirectUri);
			if( err != null ) {
				AppError error = new AppError();
				error.setNewRedirectUri(err);
				model.put("error", error);
				model.put("newRedirectUri", newRedirectUri);
				return new ModelAndView("application", model);
			}
			app.getRedirectUris().add(newRedirectUri);		// Also add value in the session list
			return new ModelAndView("application", model);
		}
		
		// Check for errors
		AppError error = this.checkApp(app);
		if( error.isError() ) {
			model.put("error", error);
			model.put("edit", true);
			return new ModelAndView("application", model);
		}
		
		// Save a new application
		if( newApp ) {
		
			// generate the secret
			String secret = this.appSvc.addApplication(app);
			model.put("secret", secret);
			
			// Display app in read only
			model.put("edit", false);
			return new ModelAndView("application", model);
		}
		
		// Update an existing application
		this.appSvc.updateApplication(app);
		return new ModelAndView("redirect:listApplications");
	}
	
	/**
	 * Removes an application
	 * @throws NotesException 
	 */
	@RequestMapping(value = "/deleteApplication", method = RequestMethod.GET)
	public ModelAndView removeApplication(@RequestParam(value = "name", required = true) String name) throws NotesException {
		this.appSvc.removeApplication(name);
		return new ModelAndView("redirect:listApplications");
	}
	
}
