package fr.asso.afer.rest.sample.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import fr.asso.afer.rest.sample.ex.NotAuthorizedException;

/**
 * Ce controlleur rattrape les exceptions
 * @author Lionel HERVIER
 */
@ControllerAdvice
public class ExceptionController {

	/**
	 * Non autorisé => 403
	 * @return une erreur 403
	 */
	@ExceptionHandler(NotAuthorizedException.class)
	public ResponseEntity<?> handleNotAuthorizedException(NotAuthorizedException e) {
		Map<String, String> resp = new HashMap<>();
		resp.put("error", e.getMessage());
		return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
	}
}
