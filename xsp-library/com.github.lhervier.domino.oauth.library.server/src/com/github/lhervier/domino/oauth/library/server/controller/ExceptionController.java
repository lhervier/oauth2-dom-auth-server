package com.github.lhervier.domino.oauth.library.server.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lotus.domino.NotesException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.github.lhervier.domino.oauth.common.model.error.GrantError;
import com.github.lhervier.domino.oauth.common.utils.QueryStringUtils;
import com.github.lhervier.domino.oauth.library.server.ex.AuthorizeException;
import com.github.lhervier.domino.oauth.library.server.ex.GrantException;
import com.github.lhervier.domino.oauth.library.server.ex.InvalidUriException;
import com.github.lhervier.domino.oauth.library.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.library.server.ex.WrongPathException;

@ControllerAdvice
public class ExceptionController {

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
	
	/**
	 * Notes exception. We cannot handle that...
	 */
	@ExceptionHandler(NotesException.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	public ModelAndView processNotesException(NotesException e) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("error", e.getMessage());
		return new ModelAndView("error", model);
	}
}