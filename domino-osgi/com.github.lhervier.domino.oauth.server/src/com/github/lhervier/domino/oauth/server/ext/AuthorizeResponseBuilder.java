package com.github.lhervier.domino.oauth.server.ext;

public class AuthorizeResponseBuilder extends BaseResponseBuilder<AuthorizeResponseBuilder, AuthorizeResponseImpl> {

	private final AuthorizeResponseImpl pending;
	
	public static final AuthorizeResponseBuilder newBuilder() {
		return new AuthorizeResponseBuilder();
	}
	
	protected AuthorizeResponseBuilder() {
		pending = new AuthorizeResponseImpl();
	}
	
	public AuthorizeResponseBuilder setContext(Object ctx) {
		pending.context = ctx;
		return this;
	}

	@Override
	protected AuthorizeResponseImpl getBuildedObject() {
		return pending;
	}

	@Override
	protected AuthorizeResponseBuilder thisObject() {
		return this;
	}
}
