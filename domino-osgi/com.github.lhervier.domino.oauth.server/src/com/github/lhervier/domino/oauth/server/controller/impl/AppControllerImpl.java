package com.github.lhervier.domino.oauth.server.controller.impl;

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
import com.github.lhervier.domino.oauth.server.controller.AppController;
import com.github.lhervier.domino.oauth.server.ex.ForbiddenException;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ex.WrongPathException;
import com.github.lhervier.domino.oauth.server.form.ApplicationForm;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.model.ClientType;
import com.github.lhervier.domino.oauth.server.services.AppService;
import com.github.lhervier.domino.oauth.server.utils.Utils;

@Controller
@UserAuth							// Only regular users can access those methods
@Roles("AppsManager")				// Users must have the AppsManager role
@Oauth2DbContext					// Controller accessible only on the oauth2.nsf context
@RequestMapping(value = "/html")
public class AppControllerImpl implements AppController {

	/**
	 * Redirect URIs attribute
	 */
	private static final String ATTR_REDIRECT_URIS = "REDIRECT_URIS";

	/**
	 * Model attributes
	 */
	private static final String MODEL_ATTR_APPS = "apps";
	private static final String MODEL_ATTR_APP = "app";
	private static final String MODEL_ATTR_EDIT = "edit";
	private static final String MODEL_ATTR_NEWAPP = "newApp";
	
	/**
	 * Actions
	 */
	private static final String ACTION_ADDREDIRECTURI = "addRedirectUri";
	
	/**
	 * View names
	 */
	private static final String VIEW_APPLICATION = "application";
	private static final String VIEW_APPLICATIONS = "applications";
	
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
	 * Returns a ClientType from its name
	 * @param name the client type name
	 * @return the client type
	 */
	private ClientType clientType(String name) {
		if( name == null )
			return ClientType.PUBLIC;
		try {
			return ClientType.valueOf(name);
		} catch(IllegalArgumentException e) {
			return ClientType.PUBLIC;
		}
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
		ret.setClientType(app.getClientType().name());
		return ret;
	}
	
	// ======================================================================================
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.controller.AppController#listApplications()
	 */
	@RequestMapping(value = "/listApplications")
	public ModelAndView listApplications() throws NotAuthorizedException, ForbiddenException, WrongPathException {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_ATTR_APPS, this.appSvc.getApplicationsNames());
		return new ModelAndView(VIEW_APPLICATIONS, model);
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.controller.AppController#createApplication()
	 */
	@RequestMapping(value = "/newApplication", method = RequestMethod.GET)
	public ModelAndView createApplication() throws NotAuthorizedException, ForbiddenException, WrongPathException {
		Map<String, Object> model = new HashMap<String, Object>();
		ApplicationForm app = fromApplication(this.appSvc.prepareApplication());
		model.put(MODEL_ATTR_APP, app);
		model.put(MODEL_ATTR_EDIT, true);
		model.put(MODEL_ATTR_NEWAPP, true);
		this.initSessionRedirectUris(app.getExistingRedirectUris());
		return new ModelAndView(VIEW_APPLICATION, model);
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.controller.AppController#editApplication(java.lang.String)
	 */
	@RequestMapping(value = "/editApplication", method = RequestMethod.GET)
	public ModelAndView editApplication(@RequestParam(value = "name", required = true) String appName) throws NotAuthorizedException, ForbiddenException, WrongPathException {
		Map<String, Object> model = new HashMap<String, Object>();
		ApplicationForm app = fromApplication(this.appSvc.getApplicationFromName(appName));
		model.put(MODEL_ATTR_APP, app);
		model.put(MODEL_ATTR_EDIT, true);
		model.put(MODEL_ATTR_NEWAPP, false);
		this.initSessionRedirectUris(app.getExistingRedirectUris());
		return new ModelAndView(VIEW_APPLICATION, model);
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.controller.AppController#viewApplication(java.lang.String)
	 */
	@RequestMapping(value = "/viewApplication", method = RequestMethod.GET)
	public ModelAndView viewApplication(@RequestParam(value = "name", required = true) String appName) throws NotAuthorizedException, ForbiddenException, WrongPathException {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_ATTR_APP, fromApplication(this.appSvc.getApplicationFromName(appName)));
		model.put(MODEL_ATTR_EDIT, false);
		model.put(MODEL_ATTR_NEWAPP, false);
		return new ModelAndView(VIEW_APPLICATION, model);
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
	 * @see com.github.lhervier.domino.oauth.server.controller.AppController#saveApplication(com.github.lhervier.domino.oauth.server.form.ApplicationForm)
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
		model.put(MODEL_ATTR_NEWAPP, newApp);
		app.setName(form.getName());
		app.setReaders(form.getReaders());
		app.setRedirectUri(form.getRedirectUri());
		app.setClientType(clientType(form.getClientType()));
		app.setRedirectUris(this.getSessionRedirectUris());
		ApplicationForm newForm = fromApplication(app);
		model.put(MODEL_ATTR_APP, newForm);
		
		// Just want to add a redirectUri
		if( Utils.equals(ACTION_ADDREDIRECTURI, form.getAction()) ) {
			
			newForm.setNewRedirectUriError(Utils.checkRedirectUri(form.getNewRedirectUri()));
			if( newForm.getNewRedirectUriError() == null ) {
				this.getSessionRedirectUris().add(form.getNewRedirectUri());		// Also add value in the session list
				newForm.setExistingRedirectUris(this.getSessionRedirectUris());
			} else
				newForm.setNewRedirectUri(form.getNewRedirectUri());				// Send value back to the browser
			
			model.put(MODEL_ATTR_EDIT, true);
			return new ModelAndView(VIEW_APPLICATION, model);
		}
		
		// Check for errors
		this.checkForm(newForm);
		if( newForm.isError() ) {
			model.put(MODEL_ATTR_EDIT, true);
			return new ModelAndView(VIEW_APPLICATION, model);
		}
		
		// Save a new application
		if( newApp ) {
		
			// generate the secret
			String secret = this.appSvc.addApplication(app);
			newForm.setSecret(secret);
			
			// Display app in read only
			model.put(MODEL_ATTR_EDIT, false);
			return new ModelAndView(VIEW_APPLICATION, model);
		}
		
		// Update an existing application => Goto the list
		this.appSvc.updateApplication(app);
		return new ModelAndView("redirect:listApplications");
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.controller.AppController#removeApplication(java.lang.String)
	 */
	@RequestMapping(value = "/deleteApplication", method = RequestMethod.GET)
	public ModelAndView removeApplication(@RequestParam(value = "name", required = true) String name) throws NotAuthorizedException, ForbiddenException, WrongPathException {
		this.appSvc.removeApplication(name);
		return new ModelAndView("redirect:listApplications");
	}
	
}
