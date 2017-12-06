package com.github.lhervier.domino.oauth.server.ext;

public class OAuthProperty {

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
