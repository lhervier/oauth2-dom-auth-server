package com.github.lhervier.domino.oauth.library.server.ext.impl;

import java.util.List;
import java.util.Map.Entry;

import lotus.domino.NotesException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.common.utils.SystemUtils;
import com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.library.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.library.server.ext.IScopeGranter;
import com.github.lhervier.domino.spring.servlet.UserSession;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * OAUTH2 Core extension
 * @author Lionel HERVIER
 */
@Component
public class CoreExt implements IOAuthExtension {

	/**
	 * The issuer
	 */
	@Value("${oauth2.server.core.iss}")
	private String iss;
	
	/**
	 * Token expiration
	 */
	@Value("${oauth2.server.core.expiresIn}")
	private long expiresIn;
	
	/**
	 * The session opened as the current user
	 */
	@Autowired
	private UserSession userSession;
	
	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#getId()
	 */
	@Override
	public String getId() {
		return "core";
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#authorize(IScopeGranter, String, List)
	 */
	@Override
	public JsonObject authorize(
			IScopeGranter granter, 
			String clientId, 
			List<String> scopes) throws NotesException {
		JsonObject attrs = new JsonObject();
		attrs.addProperty("iss", this.iss);
		attrs.addProperty("aud", clientId);
		attrs.addProperty("sub", this.userSession.getEffectiveUserName());
		attrs.addProperty("exp", SystemUtils.currentTimeSeconds() + this.expiresIn);
		return attrs;
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#token(JsonObject, IPropertyAdder)
	 */
	@Override
	public void token(
			JsonObject context, 
			IPropertyAdder adder) throws NotesException {
		JsonObject accessToken = new JsonObject();
		for( Entry<String, JsonElement> entry : context.entrySet() )
			accessToken.add(entry.getKey(), entry.getValue());
		adder.addSignedProperty("access_token", accessToken);
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#refresh(JsonObject, IPropertyAdder, IScopeGranter, List)
	 */
	@Override
	public void refresh(
			JsonObject context, 
			IPropertyAdder adder, 
			IScopeGranter granter, 
			List<String> scopes) throws NotesException {
		JsonObject accessToken = new JsonObject();
		for( Entry<String, JsonElement> entry : context.entrySet() )
			accessToken.add(entry.getKey(), entry.getValue());
		accessToken.addProperty("exp", SystemUtils.currentTimeSeconds() + this.expiresIn);
		adder.addSignedProperty("access_token", accessToken);
	}
}
