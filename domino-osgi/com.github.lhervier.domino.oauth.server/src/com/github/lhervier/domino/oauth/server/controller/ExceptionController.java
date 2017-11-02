package com.github.lhervier.domino.oauth.server.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.github.lhervier.domino.oauth.server.ex.AuthorizeException;
import com.github.lhervier.domino.oauth.server.ex.GrantException;
import com.github.lhervier.domino.oauth.server.ex.InvalidUriException;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.WrongPathException;
import com.github.lhervier.domino.oauth.server.model.error.grant.GrantError;
import com.github.lhervier.domino.oauth.server.utils.QueryStringUtils;

@ControllerAdvice
public class ExceptionController {

	/**
	 * Logger
	 */
	private static final Log LOG = LogFactory.getLog(ExceptionController.class);
	
	/**
	 * The http servlet request
	 */
	@Autowired
	private HttpServletRequest request;
	
	/**
	 * Handle Authorization errors.
	 * Redirect with error detail in url if redirect_uri is present.
	 * Throw error otherwise
	 * @param e the error
	 * @return the model and the view
	 */
	@ExceptionHandler(AuthorizeException.class)
	public ModelAndView processAuthorizedException(AuthorizeException e) throws InvalidUriException {
		// We need a redirect uri
		String redirectUri = this.request.getParameter("redirect_rui");
		if( StringUtils.isEmpty(redirectUri) )
			throw new InvalidUriException("No redirect_uri in query string.");
		
		return new ModelAndView("redirect:" + QueryStringUtils.addBeanToQueryString(redirectUri, e.getError()));
	}
	
	/**
	 * Handle Grant errors.
	 */
	@ExceptionHandler(GrantException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public @ResponseBody GrantError handleGrantException(GrantException e) {
		return e.getError();
	}
	
	/**
	 * Handle wrong path exceptions sending a 404 error
	 */
	@ExceptionHandler(WrongPathException.class)
	public ResponseEntity<Void> handleWrongPathException(WrongPathException e) {
		return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
	}
	
	/**
	 * Invalid URI exception. We cannot handle that...
	 */
	@ExceptionHandler(InvalidUriException.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	public ModelAndView processInvalidUriException(InvalidUriException e) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("error", e.getMessage());
		return new ModelAndView("error", model);
	}
	
	/**
	 * Server error exception. We cannot handle that...
	 */
	@ExceptionHandler(ServerErrorException.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	public ModelAndView processServerErrorException(ServerErrorException e) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("error", e.getMessage());
		return new ModelAndView("error", model);
	}
	
	public static class NotAuthorizedResponse {
		private String error;
		public String getError() {return this.error; }
		public void setError(String error) { this.error = error; }
	}
	/**
	 * NotAuthorizedException
	 */
	@ExceptionHandler(NotAuthorizedException.class)
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	public @ResponseBody NotAuthorizedResponse processNotAuthorizedException(NotAuthorizedException e) {
		NotAuthorizedResponse ret = new NotAuthorizedResponse();
		ret.setError("not_authorized");
		return ret;
	}
	
	/**
	 * Other exception. We cannot handle that...
	 */
	@ExceptionHandler(Throwable.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	public ModelAndView processThrowable(Throwable e) {
		LOG.error(e);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("error", e.getMessage());
		return new ModelAndView("error", model);
	}	
}
