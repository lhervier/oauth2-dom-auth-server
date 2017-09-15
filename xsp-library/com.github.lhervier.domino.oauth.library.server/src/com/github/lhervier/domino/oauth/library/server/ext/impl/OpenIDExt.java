package com.github.lhervier.domino.oauth.library.server.ext.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lotus.domino.Name;
import lotus.domino.NotesException;

import com.github.lhervier.domino.oauth.common.utils.DominoUtils;
import com.github.lhervier.domino.oauth.common.utils.SystemUtils;
import com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.library.server.ext.IPropertyAdder;
import com.github.lhervier.domino.oauth.library.server.ext.IScopeGranter;
import com.github.lhervier.domino.spring.servlet.NotesContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * Implémentation de OpenID par dessus OAUth2
 * @author Lionel HERVIER
 */
@Component
public class OpenIDExt implements IOAuthExtension {

	/**
	 * The issuer
	 */
	@Value("${oauth2.server.openid.iss}")
	private String iss;
	
	/**
	 * Id token expiration
	 */
	@Value("${oauth2.server.openid.expiresIn}")
	private long expiresIn;
	
	/**
	 * The notes context
	 */
	@Autowired
	private NotesContext notesContext;
	
	/**
	 * The http servlet request
	 */
	@Autowired
	private HttpServletRequest request;
	
	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#getId()
	 */
	@Override
	public String getId() {
		return "openid";
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#authorize(IScopeGranter, String, List)
	 */
	@Override
	public JsonObject authorize(
			IScopeGranter granter, 
			String clientId, 
			List<String> scopes) throws NotesException {
		// On ne réagit que si on nous demande le scope "openid"
		if( !scopes.contains("openid") )
			return null;
		granter.grant("openid");
		
		// Les attributs par défaut
		JsonObject attrs = new JsonObject();
		attrs.addProperty("iss", this.iss);
		attrs.addProperty("sub", this.notesContext.getUserSession().getEffectiveUserName());
		attrs.addProperty("aud", clientId);
		attrs.addProperty("exp", SystemUtils.currentTimeSeconds() + this.expiresIn);
		attrs.addProperty("auth_time", SystemUtils.currentTimeSeconds());
		attrs.addProperty("acr", "");				// TODO: acr non généré
		attrs.addProperty("amr", "");				// TODO: amr non généré
		attrs.addProperty("azp", "");				// TODO: azp non généré
		attrs.addProperty("auth_time", SystemUtils.currentTimeSeconds());
		if( this.request.getParameter("nonce") != null )
			attrs.addProperty("nonce", this.request.getParameter("nonce"));
		else
			attrs.add("nonce", JsonNull.INSTANCE);
		
		if( scopes.contains("profile") ) {
			granter.grant("profile");
			
			Name nn = null;
			try {
				nn = this.notesContext.getUserSession().createName(this.notesContext.getUserSession().getEffectiveUserName());
				attrs.addProperty("name", nn.getCommon());
			} finally {
				DominoUtils.recycleQuietly(nn);
			}
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
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#token(JsonObject, IPropertyAdder)
	 */
	@Override
	public void token(
			JsonObject context, 
			IPropertyAdder adder) {
		JsonObject idToken = new JsonObject();
		for( Entry<String, JsonElement> entry : context.entrySet() )
			idToken.add(entry.getKey(), entry.getValue());
		idToken.addProperty("iat", SystemUtils.currentTimeSeconds());
		
		adder.addSignedProperty("id_token", idToken);
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.server.ext.IOAuthExtension#refresh(JsonObject, IPropertyAdder, IScopeGranter, List)
	 */
	@Override
	public void refresh(
			JsonObject context, 
			IPropertyAdder adder, 
			IScopeGranter granter, 
			List<String> scopes) {
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
		
		this.token(context, adder);
	}
}
