package com.github.lhervier.domino.oauth.server.ext;

public class AuthorizeResponseImpl extends BaseResponse implements AuthorizeResponse {

	Object context;
	
	protected AuthorizeResponseImpl() {}
	
	public Object getContext() {
		return context;
	}
}
