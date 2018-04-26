package com.github.lhervier.domino.oauth.server.controller.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.lhervier.domino.oauth.server.aop.ann.ctx.Oauth2DbContext;
import com.github.lhervier.domino.oauth.server.aop.ann.security.Roles;
import com.github.lhervier.domino.oauth.server.aop.ann.security.UserAuth;
import com.github.lhervier.domino.oauth.server.controller.APIController;
import com.github.lhervier.domino.oauth.server.ex.APIException;
import com.github.lhervier.domino.oauth.server.ex.ForbiddenException;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ex.WrongPathException;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.model.BaseApplication;
import com.github.lhervier.domino.oauth.server.services.AppService;

/**
 * Rest controller for APIs access
 * @author Lionel HERVIER
 */
@Controller
@Oauth2DbContext			// End points avaliable at the oauth2-db only
@UserAuth					// Must be logged in as a user (and not an app)
@Roles("AppsManager")		// User must have the AppsManager role
@RequestMapping(value = "/api")
public class APIControllerImpl implements APIController {

	/**
	 * The app service
	 */
	@Autowired
	private AppService appSvc;
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.controller.APIController#listApplications()
	 */
	@Override
	@RequestMapping(value = "/applications", method = RequestMethod.GET)
	public @ResponseBody List<BaseApplication> listApplications() throws NotAuthorizedException, ForbiddenException, WrongPathException, APIException {
		List<String> names = this.appSvc.getApplicationsNames();
		List<BaseApplication> ret = new ArrayList<BaseApplication>();
		for( String name : names ) {
			Application app = this.appSvc.getApplicationFromName(name);
			BaseApplication obj = new BaseApplication();
			obj.setName(name);
			obj.setClientId(app.getClientId());
			ret.add(obj);
		}
		return ret;
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.controller.APIController#getApplication(java.lang.String)
	 */
	@Override
	@RequestMapping(value = "/applications/{clientId}", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<Application> getApplication(@PathVariable String clientId) throws NotAuthorizedException, ForbiddenException, WrongPathException, APIException {
		Application app = this.appSvc.getApplicationFromClientId(clientId);
		if( app == null )
			return new ResponseEntity<Application>(HttpStatus.NOT_FOUND);
		return new ResponseEntity<Application>(app, HttpStatus.OK);
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.controller.APIController#newApplication(com.github.lhervier.domino.oauth.server.model.Application)
	 */
	@Override
	@RequestMapping(value = "/applications", method = RequestMethod.POST)
	public @ResponseBody NewApplicationResponse newApplication(@RequestBody Application app) throws NotAuthorizedException, ForbiddenException, WrongPathException, APIException {
		try {
			String clientId = UUID.randomUUID().toString();
			app.setClientId(clientId);
			String secret = this.appSvc.addApplication(app);
			NewApplicationResponse ret = new NewApplicationResponse();
			ret.setClientId(clientId);
			ret.setSecret(secret);
			return ret;
		} catch(Exception e) {
			throw new APIException(e.getMessage(), e);
		}
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.controller.APIController#updateApplication(java.lang.String, com.github.lhervier.domino.oauth.server.model.Application)
	 * We are using a GET request insted of a normal PUT request because such requests are deactivated by
	 * default on Domino Servers.
	 */
	@Override
	@RequestMapping(value = "/applications/{clientId}/put", method = RequestMethod.POST)
	public @ResponseBody Map<String, String> updateApplication(@PathVariable String clientId, @RequestBody Application app) throws NotAuthorizedException, ForbiddenException, WrongPathException, APIException {
		try {
			app.setClientId(clientId);
			this.appSvc.updateApplication(app);
			return new HashMap<String, String>();
		} catch(Exception e) {
			throw new APIException(e.getMessage(), e);
		}
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.controller.APIController#removeApplication(java.lang.String)
	 * We are using a GET request insted of a normal DELETE request because such requests are deactivated by
	 * default on Domino Servers.
	 */
	@Override
	@RequestMapping(value = "/applications/{clientId}/delete", method = RequestMethod.GET)
	public @ResponseBody Map<String, String> removeApplication(@PathVariable String clientId) throws NotAuthorizedException, ForbiddenException, WrongPathException, APIException {
		try {
			this.appSvc.removeApplicationFromClientId(clientId);
			return new HashMap<String, String>();
		} catch(Exception e) {
			throw new APIException(e.getMessage(), e);
		}
	}
}
