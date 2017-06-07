package com.github.lhervier.domino.oauth.library.server.model.error.grant;

import com.github.lhervier.domino.oauth.common.model.error.GrantError;
import com.github.lhervier.domino.oauth.library.server.utils.Utils;

public class InvalidClientError extends GrantError {

	/**
	 * Constructeur
	 */
	public InvalidClientError() {
		this.setError("invalid_client");
		this.setErrorDescription(
				"Client authentication failed (e.g., unknown client, no " + 
				"client authentication included, or unsupported " + 
				"authentication method).  The authorization server MAY " + 
				"return an HTTP 401 (Unauthorized) status code to indicate " + 
				"which HTTP authentication schemes are supported.  If the " + 
				"client attempted to authenticate via the \"Authorization\" " + 
				"request header field, the authorization server MUST " + 
				"respond with an HTTP 401 (Unauthorized) status code and " + 
				"include the \"WWW-Authenticate\" response header field " + 
				"matching the authentication scheme used by the client."
		);
		this.setErrorUri(Utils.getIssuer() + "/error/grant/invalid_client");
	}
}
