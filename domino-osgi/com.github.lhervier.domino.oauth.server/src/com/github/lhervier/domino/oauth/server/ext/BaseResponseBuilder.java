package com.github.lhervier.domino.oauth.server.ext;

import com.github.lhervier.domino.oauth.server.utils.Utils;

public abstract class BaseResponseBuilder<B extends BaseResponseBuilder<B, R>, R extends BaseResponse> {

	private OAuthProperty currProperty;
	
	protected abstract R getBuildedObject();
	protected abstract B thisObject();
	
	private void persist() {
		if( this.currProperty == null )
			return;
		if( this.currProperty.getName() == null )
			throw new RuntimeException("Property must have a name");
		getBuildedObject().properties.put(this.currProperty.getName(), this.currProperty);
	}
	
	public R build() {
		this.persist();
		return getBuildedObject();
	}
	
	public B addProperty() {
		this.persist();
		this.currProperty = new OAuthProperty();
		return thisObject();
	}
	public B withName(String name) {
		if( this.currProperty == null )
			throw new RuntimeException("You must add a property before giving it a name...");
		if( Utils.equals("code", name) )
			throw new RuntimeException("'code' is a restricted property name...");
		this.currProperty.setName(name);
		return thisObject();
	}
	public B withValue(Object value) {
		if( this.currProperty == null )
			throw new RuntimeException("You must add a property before assigning it a value...");
		if( Utils.equals("code", this.currProperty.getName()) )
			throw new RuntimeException("You cannot set the value of the property that will contain the AuthCode");
		this.currProperty.setValue(value);
		return thisObject();
	}
	public B signedWith(String signKey) {
		if( this.currProperty == null )
			throw new RuntimeException("You must add a property before deciding if it must be signed...");
		if( Utils.equals("code", this.currProperty.getName()) )
			throw new RuntimeException("You cannot decide to sign the auth code property...");
		this.currProperty.setSignKey(signKey);
		return thisObject();
	}
	public B addAuthCode() {
		if( getBuildedObject().properties.containsKey("code") )
			throw new RuntimeException("AuthCode already included in response...");
		this.addProperty();
		this.currProperty.setName("code");
		return thisObject();
	}
}
