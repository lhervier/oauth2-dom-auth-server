package com.github.lhervier.domino.oauth.server.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
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
	 * Domino v9.0.1 no longer trace stack traces...
	 * @return the stack trace
	 */
	private String getStackTrace(Throwable e) {
		StringWriter wrt = new StringWriter();
		PrintWriter pw = new PrintWriter(wrt);
		e.printStackTrace(pw);
		return wrt.toString();
	}
	
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
		LOG.error(getStackTrace(e));
		return e.getError();
	}
	
	/**
	 * Handle wrong path exceptions sending a 404 error
	 */
	@ExceptionHandler(WrongPathException.class)
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public ModelAndView handleWrongPathException(WrongPathException e) {
		LOG.info(getStackTrace(e));
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("error", e);
		model.put("status", HttpStatus.NOT_FOUND.value());
		return new ModelAndView("error", model);
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
		LOG.info(getStackTrace(e));
		response.addHeader("Access-Control-Allow-Origin", "*");
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("error", e.getMessage());
		model.put("status", HttpStatus.UNAUTHORIZED.value());
		return new ModelAndView("error", model);
	}
	
	/**
	 * ForbiddenException. 
	 * This is needed to make the browser get the http status...
	 */
	@ExceptionHandler(ForbiddenException.class)
	@ResponseStatus(value = HttpStatus.FORBIDDEN)
	public ModelAndView processForbiddenException(ForbiddenException e) {
		LOG.info(getStackTrace(e));
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("error", e.getMessage());
		model.put("status", HttpStatus.FORBIDDEN.value());
		return new ModelAndView("error", model);
	}
	
	// ==========================================================================================
	
	/**
	 * Other exception. We cannot handle that...
	 */
	@ExceptionHandler(Throwable.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	public ModelAndView processThrowable(Throwable e) {
		LOG.fatal(getStackTrace(e));
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("error", e.getMessage());
		model.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
		return new ModelAndView("error", model);
	}	
}
