package com.github.lhervier.domino.oauth.ext.core;

import java.util.List;
import java.util.Map.Entry;

import lotus.domino.NotesException;
import lotus.domino.Session;

import com.github.lhervier.domino.oauth.common.utils.SystemUtils;
import com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.library.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.library.server.ext.IScopeGranter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * OAUTH Core extension
 * @author Lionel HERVIER
 */
public class CoreExt implements IOAuthExtension {

	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#getId()
	 */
	@Override
	public String getId() {
		return Activator.PLUGIN_ID;
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#authorize(Session, JsonObject, IScopeGranter, String, List)
	 */
	@Override
	public JsonObject authorize(Session userSession, JsonObject conf, IScopeGranter granter, String clientId, List<String> scopes) throws NotesException {
		JsonObject attrs = new JsonObject();
		attrs.addProperty("iss", conf.get("iss").getAsString());
		attrs.addProperty("aud", clientId);
		attrs.addProperty("sub", userSession.getEffectiveUserName());
		attrs.addProperty("exp", SystemUtils.currentTimeSeconds() + conf.get("expires_in").getAsLong());
		return attrs;
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#token(Session, JsonObject, JsonObject, IPropertyAdder)
	 */
	@Override
	public void token(Session appSession, JsonObject conf, JsonObject context, IPropertyAdder adder) throws NotesException {
		JsonObject accessToken = new JsonObject();
		for( Entry<String, JsonElement> entry : context.entrySet() )
			accessToken.add(entry.getKey(), entry.getValue());
		adder.addSignedProperty("access_token", accessToken);
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#refresh(Session, JsonObject, JsonObject, IPropertyAdder, IScopeGranter, List)
	 */
	@Override
	public void refresh(Session appSession, JsonObject conf, JsonObject context, IPropertyAdder adder, IScopeGranter granter, List<String> scopes) throws NotesException {
		this.token(appSession, conf, context, adder);
	}
}
