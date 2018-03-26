package com.github.lhervier.domino.oauth.server.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

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
import com.github.lhervier.domino.oauth.server.aop.ann.security.UserAuth;
import com.github.lhervier.domino.oauth.server.ex.ForbiddenException;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ex.WrongPathException;
import com.github.lhervier.domino.oauth.server.form.ApplicationForm;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.model.ClientType;
import com.github.lhervier.domino.oauth.server.services.AppService;
import com.github.lhervier.domino.oauth.server.utils.Utils;

@Controller
@RequestMapping(value = "/html")

@UserAuth							// Only regular users can access those methods
@Roles("AppsManager")				// Users must have the AppsManager role
@Oauth2DbContext					// Controller accessible only on the oauth2.nsf context
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
	 * Return the redirect uris
	 */
	@SuppressWarnings("unchecked")
	private List<String> getSessionRedirectUris() {
		if( this.session.getAttribute(ATTR_REDIRECT_URIS) == null )
			return new ArrayList<String>();
		return (List<String>) this.session.getAttribute(ATTR_REDIRECT_URIS);
	}
	
	/**
	 * Initialize the redirect uris
	 */
	private void initSessionRedirectUris(List<String> redirectUris) {
		this.session.setAttribute(ATTR_REDIRECT_URIS, redirectUris);
	}
	
	/**
	 * Convert an application to a form
	 */
	private ApplicationForm fromApplication(Application app) {
		ApplicationForm ret = new ApplicationForm();
		ret.setClientId(app.getClientId());
		ret.setName(app.getName());
		ret.setReaders(app.getReaders());
		ret.setRedirectUri(app.getRedirectUri());
		ret.setExistingRedirectUris(app.getRedirectUris());
		if( app.getClientType() == ClientType.CONFIDENTIAL )
			ret.setClientType("CONFIDENTIAL");
		else if( app.getClientType() == ClientType.PUBLIC )
			ret.setClientType("PUBLIC");
		else
			ret.setClientType("PUBLIC");
		return ret;
	}
	
	// ======================================================================================
	
	/**
	 * List applications
	 */
	@RequestMapping(value = "/listApplications")
	public ModelAndView listApplications() throws NotAuthorizedException, ForbiddenException, WrongPathException {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("apps", this.appSvc.getApplicationsNames());
		return new ModelAndView("applications", model);
	}
	
	/**
	 * The screen to display the creation of a new application
	 */
	@RequestMapping(value = "/newApplication", method = RequestMethod.GET)
	public ModelAndView createApplication() throws NotAuthorizedException, ForbiddenException, WrongPathException {
		Map<String, Object> model = new HashMap<String, Object>();
		ApplicationForm app = fromApplication(this.appSvc.prepareApplication());
		model.put("app", app);
		model.put("edit", true);
		model.put("newApp", true);
		this.initSessionRedirectUris(app.getExistingRedirectUris());
		return new ModelAndView("application", model);
	}
	
	/**
	 * Edits an application
	 */
	@RequestMapping(value = "/editApplication", method = RequestMethod.GET)
	public ModelAndView editApplication(@RequestParam(value = "name", required = true) String appName) throws NotAuthorizedException, ForbiddenException, WrongPathException {
		Map<String, Object> model = new HashMap<String, Object>();
		ApplicationForm app = fromApplication(this.appSvc.getApplicationFromName(appName));
		model.put("app", app);
		model.put("edit", true);
		model.put("newApp", false);
		this.initSessionRedirectUris(app.getExistingRedirectUris());
		return new ModelAndView("application", model);
	}
	
	/**
	 * Display the details of an application
	 */
	@RequestMapping(value = "/viewApplication", method = RequestMethod.GET)
	public ModelAndView viewApplication(@RequestParam(value = "name", required = true) String appName) throws NotAuthorizedException, ForbiddenException, WrongPathException {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("app", fromApplication(this.appSvc.getApplicationFromName(appName)));
		model.put("edit", false);
		model.put("newApp", false);
		return new ModelAndView("application", model);
	}
	
	// ===============================================================
	
	/**
	 * Check an application object
	 * @return an error object
	 */
	private void checkForm(ApplicationForm app) {
		if( StringUtils.isEmpty(app.getName()) )
			app.setNameError("name is mandatory");
		if( StringUtils.isEmpty(app.getReaders()) )
			app.setReadersError("readers are mandatory");
		
		app.setRedirectUriError(Utils.checkRedirectUri(app.getRedirectUri()));
	}
	
	/**
	 * Save an application
	 */
	@RequestMapping(value = "/saveApplication", method = RequestMethod.POST)
	public ModelAndView saveApplication(@ModelAttribute ApplicationForm form) throws NotAuthorizedException, ForbiddenException, WrongPathException {
		Map<String, Object> model = new HashMap<String, Object>();
		
		// Apply changes (validation later)
		Application app = this.appSvc.getApplicationFromClientId(form.getClientId());
		boolean newApp = false;
		if( app == null ) {
			app = this.appSvc.prepareApplication();		// Will generate a new clientId !!
			app.setClientId(form.getClientId());
			newApp = true;
		}
		model.put("newApp", newApp);
		app.setName(form.getName());
		app.setReaders(form.getReaders());
		app.setRedirectUri(form.getRedirectUri());
		if( Utils.equals("CONFIDENTIAL", form.getClientType()) )
			app.setClientType(ClientType.CONFIDENTIAL);
		else if( Utils.equals("PUBLIC", form.getClientType()) )
			app.setClientType(ClientType.PUBLIC);
		else
			app.setClientType(ClientType.PUBLIC);
		app.setRedirectUris(this.getSessionRedirectUris());
		ApplicationForm newForm = fromApplication(app);
		model.put("app", newForm);
		
		// Just want to add a redirectUri
		if( Utils.equals("addRedirectUri", form.getAction()) ) {
			
			newForm.setNewRedirectUriError(Utils.checkRedirectUri(form.getNewRedirectUri()));
			if( newForm.getNewRedirectUriError() == null ) {
				this.getSessionRedirectUris().add(form.getNewRedirectUri());		// Also add value in the session list
				newForm.setExistingRedirectUris(this.getSessionRedirectUris());
			} else
				newForm.setNewRedirectUri(form.getNewRedirectUri());				// Send value back to the browser
			
			model.put("edit", true);
			return new ModelAndView("application", model);
		}
		
		// Check for errors
		this.checkForm(newForm);
		if( newForm.isError() ) {
			model.put("edit", true);
			return new ModelAndView("application", model);
		}
		
		// Save a new application
		if( newApp ) {
		
			// generate the secret
			String secret = this.appSvc.addApplication(app);
			newForm.setSecret(secret);
			
			// Display app in read only
			model.put("edit", false);
			return new ModelAndView("application", model);
		}
		
		// Update an existing application => Goto the list
		this.appSvc.updateApplication(app);
		return new ModelAndView("redirect:listApplications");
	}
	
	/**
	 * Removes an application
	 */
	@RequestMapping(value = "/deleteApplication", method = RequestMethod.GET)
	public ModelAndView removeApplication(@RequestParam(value = "name", required = true) String name) throws NotAuthorizedException, ForbiddenException, WrongPathException {
		this.appSvc.removeApplication(name);
		return new ModelAndView("redirect:listApplications");
	}
	
}
