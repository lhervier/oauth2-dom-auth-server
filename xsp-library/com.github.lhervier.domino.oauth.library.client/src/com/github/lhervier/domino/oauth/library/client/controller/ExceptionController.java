package com.github.lhervier.domino.oauth.library.client.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.github.lhervier.domino.oauth.library.client.ex.OauthClientException;
import com.github.lhervier.domino.oauth.library.client.ex.WrongPathException;

@ControllerAdvice
public class ExceptionController {

	/**
	 * Handle wrong path exceptions sending a 404 error
	 */
	@ExceptionHandler(WrongPathException.class)
	public ResponseEntity<Void> handleWrongPathException(WrongPathException e) {
		return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
	}
	
	/**
	 * Server error exception. We cannot handle that...
	 */
	@ExceptionHandler(OauthClientException.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	public ModelAndView processServerErrorException(OauthClientException e) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("error", e.getMessage());
		return new ModelAndView("error", model);
	}
}
