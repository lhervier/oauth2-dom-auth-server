package com.github.lhervier.domino.oauth.server.ext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.lhervier.domino.oauth.server.utils.Utils;

public class AuthorizeResponse {

	public static class OAuthProperty {
		private String name;
		private Object value;
		private String signKey;
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		public Object getValue() { return value; }
		public void setValue(Object value) { this.value = value; }
		public String getSignKey() { return signKey; }
		public void setSignKey(String signKey) { this.signKey = signKey; }
	}
	
	private List<OAuthProperty> properties = new ArrayList<OAuthProperty>();
	private Object context;
	
	public List<OAuthProperty> getProperties() { return properties; }
	public Object getContext() { return context; }

	public static final AuthorizeResponseBuilder init() {
		return new AuthorizeResponseBuilder();
	}
	
	public static class AuthorizeResponseBuilder {
		private Map<String, OAuthProperty> properties = new HashMap<String, OAuthProperty>();
		private Object context = null;
		private OAuthProperty currProperty = null;
		private AuthorizeResponseBuilder() {}
		public AuthorizeResponse build() {
			this.persist();
			AuthorizeResponse resp = new AuthorizeResponse();
			resp.properties.addAll(this.properties.values());
			resp.context = this.context;
			return resp;
		}
		private void persist() {
			if( this.currProperty == null )
				return;
			if( this.currProperty.getName() == null )
				throw new RuntimeException("Property must have a name");
			this.properties.put(this.currProperty.getName(), this.currProperty);
		}
		public AuthorizeResponseBuilder setContext(Object ctx) {
			this.context = ctx;
			return this;
		}
		public AuthorizeResponseBuilder addProperty() {
			this.persist();
			this.currProperty = new OAuthProperty();
			return this;
		}
		public AuthorizeResponseBuilder withName(String name) {
			if( this.currProperty == null )
				throw new RuntimeException("You must add a property before giving it a name...");
			if( Utils.equals("code", name) )
				throw new RuntimeException("'code' is a restricted property name...");
			this.currProperty.setName(name);
			return this;
		}
		public AuthorizeResponseBuilder withValue(Object value) {
			if( this.currProperty == null )
				throw new RuntimeException("You must add a property before assigning it a value...");
			if( Utils.equals("code", this.currProperty.getName()) )
				throw new RuntimeException("You cannot set the value of the property that will contain the AuthCode");
			this.currProperty.setValue(value);
			return this;
		}
		public AuthorizeResponseBuilder signedWith(String signKey) {
			if( this.currProperty == null )
				throw new RuntimeException("You must add a property before deciding if it must be signed...");
			if( Utils.equals("code", this.currProperty.getName()) )
				throw new RuntimeException("You cannot decide to sign the auth code property...");
			this.currProperty.setSignKey(signKey);
			return this;
		}
		public AuthorizeResponseBuilder addAuthCode() {
			if( this.properties.containsKey("code") )
				throw new RuntimeException("AuthCode already included in response...");
			this.addProperty();
			this.currProperty.setName("code");
			return this;
		}
	}
}
