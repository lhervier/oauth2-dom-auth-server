package com.github.lhervier.domino.oauth.server.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.github.lhervier.domino.oauth.server.ex.BaseAuthException;
import com.github.lhervier.domino.oauth.server.ex.BaseGrantException;
import com.github.lhervier.domino.oauth.server.ex.ForbiddenException;
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
	 * Handle Authorization errors.
	 * Redirect with error detail in url if redirect_uri is present.
	 * Throw error otherwise
	 * @param e the error
	 * @return the model and the view
	 */
	@ExceptionHandler(BaseAuthException.class)
	@ResponseStatus(value = HttpStatus.OK)
	public ModelAndView processAuthorizedException(BaseAuthException e) throws InvalidUriException {
		LOG.error(e.getMessage());
		return new ModelAndView("redirect:" + QueryStringUtils.addBeanToQueryString(e.getRedirectUri(), e.getError()));
	}
	
	/**
	 * Handle Grant errors.
	 */
	@ExceptionHandler(BaseGrantException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public @ResponseBody GrantError handleGrantException(BaseGrantException e) {
		LOG.error(e.getMessage());
		return e.getError();
	}
	
	/**
	 * Handle wrong path exceptions sending a 404 error
	 */
	@ExceptionHandler(WrongPathException.class)
	public ResponseEntity<Void> handleWrongPathException(WrongPathException e) {
		return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
	}
	
	// ======================================================================================
	
	/**
	 * NotAuthorizedException. 
	 * FIXME: Sending cors headers here, event when it is not a Cors request...
	 * This is needed to make the browser get the http status...
	 */
	@ExceptionHandler(NotAuthorizedException.class)
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	public ModelAndView processNotAuthorizedException(NotAuthorizedException e, HttpServletResponse response) {
		LOG.info(e);
		response.addHeader("Access-Control-Allow-Origin", "*");
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("error", e.getMessage());
		return new ModelAndView("error", model);
	}
	
	/**
	 * ForbiddenException. 
	 * This is needed to make the browser get the http status...
	 */
	@ExceptionHandler(ForbiddenException.class)
	@ResponseStatus(value = HttpStatus.FORBIDDEN)
	public ModelAndView processForbiddenException(ForbiddenException e) {
		LOG.info(e);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("error", e.getMessage());
		return new ModelAndView("error", model);
	}
	
	/**
	 * Invalid URI exception. We cannot handle that...
	 */
	@ExceptionHandler(InvalidUriException.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	public ModelAndView processInvalidUriException(InvalidUriException e) {
		LOG.fatal(e);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("error", e.getMessage());
		return new ModelAndView("error", model);
	}
	
	// ========================================================================================
	
	/**
	 * Data access exception. We cannot handle that...
	 */
	@ExceptionHandler(DataAccessException.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	public ModelAndView processDataAccessException(DataAccessException e) {
		LOG.fatal(e);
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
		LOG.error(e);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("error", e.getMessage());
		return new ModelAndView("error", model);
	}
	
	// ==========================================================================================
	
	/**
	 * Other exception. We cannot handle that...
	 */
	@ExceptionHandler(Throwable.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	public ModelAndView processThrowable(Throwable e) {
		LOG.fatal(e);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("error", e.getMessage());
		return new ModelAndView("error", model);
	}	
}
