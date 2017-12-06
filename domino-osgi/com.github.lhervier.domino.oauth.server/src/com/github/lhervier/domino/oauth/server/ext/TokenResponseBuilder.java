package com.github.lhervier.domino.oauth.server.ext;

public class TokenResponseBuilder extends BaseResponseBuilder<TokenResponseBuilder, TokenResponseImpl> {

	public static final TokenResponseBuilder newBuilder() {
		return new TokenResponseBuilder();
	}
	
	private final TokenResponseImpl pending;
	
	protected TokenResponseBuilder() {
		pending = new TokenResponseImpl();
	}
	
	@Override
	public TokenResponseImpl getBuildedObject() {
		return pending;
	}

	@Override
	protected TokenResponseBuilder thisObject() {
		return this;
	}
}
