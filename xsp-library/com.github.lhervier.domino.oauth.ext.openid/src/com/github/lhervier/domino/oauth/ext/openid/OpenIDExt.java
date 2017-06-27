package com.github.lhervier.domino.oauth.ext.openid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import lotus.domino.NotesException;

import com.github.lhervier.domino.oauth.common.utils.JSFUtils;
import com.github.lhervier.domino.oauth.common.utils.SystemUtils;
import com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.library.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.library.server.ext.IScopeGranter;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * Implémentation de OpenID par dessus OAUth2
 * @author Lionel HERVIER
 */
public class OpenIDExt implements IOAuthExtension {

	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#getId()
	 */
	@Override
	public String getId() {
		return Activator.PLUGIN_ID;
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#authorize(JsonObject, IScopeGranter, String, List)
	 */
	@Override
	public JsonObject authorize(JsonObject conf, IScopeGranter granter, String clientId, List<String> scopes) throws NotesException {
		// On ne réagit que si on nous demande le scope "openid"
		if( !scopes.contains("openid") )
			return null;
		granter.grant("openid");
		
		// Les attributs par défaut
		JsonObject attrs = new JsonObject();
		attrs.addProperty("iss", conf.get("iss").getAsString());
		attrs.addProperty("sub", JSFUtils.getSession().getEffectiveUserName());
		attrs.addProperty("aud", clientId);
		attrs.addProperty("exp", SystemUtils.currentTimeSeconds() + conf.get("expires_in").getAsLong());
		attrs.addProperty("auth_time", SystemUtils.currentTimeSeconds());
		attrs.addProperty("acr", "");				// TODO: acr non généré
		attrs.addProperty("amr", "");				// TODO: amr non généré
		attrs.addProperty("azp", "");				// TODO: azp non généré
		attrs.addProperty("auth_time", SystemUtils.currentTimeSeconds());
		if( JSFUtils.getParam().get("nonce") != null )
			attrs.addProperty("nonce", JSFUtils.getParam().get("nonce"));
		else
			attrs.add("nonce", JsonNull.INSTANCE);
		
		if( scopes.contains("profile") ) {
			granter.grant("profile");
			
			attrs.addProperty("name", JSFUtils.getSession().getCommonUserName());
			// TODO: Récupérer ces infos depuis la fiche du NAB !!
			attrs.addProperty("family_name", "");
			attrs.addProperty("given_name", "");
			attrs.addProperty("middle_name", "");
			attrs.addProperty("nickname", "");
			attrs.addProperty("preferred_username", "");
			attrs.addProperty("profile", "");
			attrs.addProperty("picture", "");
			attrs.addProperty("website", "");
			attrs.addProperty("gender", "");
			attrs.addProperty("birthdate", "");
			attrs.addProperty("zoneinfo", "");
			attrs.addProperty("locale", "");
			attrs.addProperty("updated_at", "");
		}
		
		if( scopes.contains("email") ) {
			granter.grant("email");
			attrs.addProperty("email", "");
			attrs.addProperty("email_verified", "");
		}
		
		if( scopes.contains("address") ) {
			granter.grant("address");
			attrs.addProperty("address", "");
		}
		
		if( scopes.contains("phone") ) {
			granter.grant("phone");
			attrs.addProperty("phone_number", "");
			attrs.addProperty("phone_number_verified", "");
		}
		
		return attrs;
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#token(JsonObject, JsonObject, IPropertyAdder)
	 */
	@Override
	public void token(JsonObject conf, JsonObject context, IPropertyAdder adder) {
		JsonObject idToken = new JsonObject();
		for( Entry<String, JsonElement> entry : context.entrySet() )
			idToken.add(entry.getKey(), entry.getValue());
		idToken.addProperty("iat", SystemUtils.currentTimeSeconds());
		
		adder.addSignedProperty("id_token", idToken);
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#refresh(JsonObject, JsonObject, IPropertyAdder, IScopeGranter, List)
	 */
	@Override
	public void refresh(JsonObject conf, JsonObject context, IPropertyAdder adder, IScopeGranter granter, List<String> scopes) {
		if( !scopes.contains("openid") ) {
			List<String> props = new ArrayList<String>();
			for( Entry<String, JsonElement> entry : context.entrySet() )
				props.add(entry.getKey());
			for( String prop : props )
				context.remove(prop);
			return;
		} else
			granter.grant("openid");
		
		if( !scopes.contains("profile") ) {
			context.remove("name");
			context.remove("family_name");
			context.remove("given_name");
			context.remove("middle_name");
			context.remove("nickname");
			context.remove("preferred_username");
			context.remove("profile");
			context.remove("picture");
			context.remove("website");
			context.remove("gender");
			context.remove("birthdate");
			context.remove("zoneinfo");
			context.remove("locale");
			context.remove("updated_at");
		} else
			granter.grant("profile");
		
		if( !scopes.contains("email") ) {
			context.remove("email");
			context.remove("email_verified");
		} else
			granter.grant("email");
		
		if( !scopes.contains("address") ) {
			context.remove("address");
		} else
			granter.grant("address");
		
		if( !scopes.contains("phone") ) {
			context.remove("phone_number");
			context.remove("phone_number_verified");
		} else
			granter.grant("phone");
		
		this.token(conf, context, adder);
	}
}
