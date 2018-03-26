package com.github.lhervier.domino.oauth.server.controller;

import javax.servlet.http.HttpServletResponse;

import com.github.lhervier.domino.oauth.server.ex.ForbiddenException;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ex.WrongPathException;
import com.github.lhervier.domino.oauth.server.model.TokenContent;

/**
 * Token introspection endpoint;
 * See https://tools.ietf.org/html/rfc7662
 * @author Lionel HERVIER
 */
public interface CheckTokenController {

	/**
	 * Check a given token
	 * @param token the token
	 * @return the token content
	 */
	public TokenContent checkToken(
			String token,
			HttpServletResponse response) throws NotAuthorizedException, ForbiddenException, WrongPathException;
}
