package com.github.lhervier.domino.oauth.server.controller;

import javax.servlet.http.HttpServletResponse;

import com.github.lhervier.domino.oauth.server.ex.ForbiddenException;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ex.WrongPathException;
import com.github.lhervier.domino.oauth.server.ext.openid.IdToken;

/**
 * Controller to manage userInfo openId end point
 * @author Lionel HERVIER
 */
public interface OpenIdController {

	/**
	 * The userInfo end point
	 * @return
	 */
	public IdToken userInfo(HttpServletResponse response) throws NotAuthorizedException, ForbiddenException, WrongPathException;
}
