package com.github.lhervier.domino.oauth.server.controller;

import java.util.Map;

import com.github.lhervier.domino.oauth.server.NotesPrincipal;
import com.github.lhervier.domino.oauth.server.ex.BaseGrantException;
import com.github.lhervier.domino.oauth.server.ex.ForbiddenException;
import com.github.lhervier.domino.oauth.server.ex.NotAuthorizedException;
import com.github.lhervier.domino.oauth.server.ex.ServerErrorException;
import com.github.lhervier.domino.oauth.server.ex.WrongPathException;

/**
 * Bean pour le endpoint "token"
 * @author Lionel HERVIER
 */
public interface TokenController {

	/**
	 * Generate a token
	 * @throws BaseGrantException error that must be serialized to the user
	 * @throws ServerErrorException main error
	 */
	public Map<String, Object> token(
			NotesPrincipal user,
			String clientId,
			String grantType) throws NotAuthorizedException, ForbiddenException, WrongPathException, BaseGrantException, ServerErrorException;
}
