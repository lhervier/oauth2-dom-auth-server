package com.github.lhervier.domino.oauth.common.utils;

import java.security.AccessController;
import java.security.PrivilegedAction;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GsonUtils {

	/**
	 * Pour transformer un objet en json
	 * @param obj l'objet
	 * @return le JSON
	 */
	public static final String toJson(final Object obj) {
		return AccessController.doPrivileged(new PrivilegedAction<String>() {
			@Override
			public String run() {
				return new Gson().toJson(obj);
			}
		});
	}
	
	/**
	 * Pour transformer du json en un objet
	 * @param json le json
	 * @param cl le type de l'objet
	 * @return l'objet
	 */
	public static final <T> T fromJson(final String json, final Class<T> cl) {
		return AccessController.doPrivileged(new PrivilegedAction<T>() {
			@Override
			public T run() {
				return new Gson().fromJson(json, cl);
			}
		});
	}
	
	/**
	 * Pour transformer du json en un objet
	 * @param json le json
	 * @return l'objet
	 */
	public static final JsonObject fromJson(final String json) {
		return AccessController.doPrivileged(new PrivilegedAction<JsonObject>() {
			@Override
			public JsonObject run() {
				return new JsonParser().parse(json).getAsJsonObject();
			}
		});
	}
}
