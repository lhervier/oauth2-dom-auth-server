package com.github.lhervier.domino.oauth.common.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * Méthodes pratiques pour JSF
 * @author Lionel HERVIER
 */
public class HttpUtils {

	/**
	 * Envoi du Json en réponse dans la stream HTTP
	 * @param response the http response
	 * @param obj l'objet à envoyer
	 * @throws IOException 
	 */
	public static final void sendJson(HttpServletResponse response, Object o) throws IOException {
		OutputStream out = null;
		OutputStreamWriter wrt = null;
		try {
			response.setContentType("application/json;charset=UTF-8");
			response.setHeader("Cache-Control", "no-store");
			response.setHeader("Pragma", "no-cache");
			if( o != null ) {
				out = response.getOutputStream();
				wrt = new OutputStreamWriter(out, "UTF-8");
				wrt.write(GsonUtils.toJson(o));
			}
		} finally {
			IOUtils.closeQuietly(wrt);
			IOUtils.closeQuietly(out);
		}
	}
	
	/**
	 * Redirige
	 * @param response the http response
	 * @param url l'url où rediriger
	 */
	public static final void sendRedirect(HttpServletResponse response, String location) {
		response.setStatus(302);
		response.setHeader("Location", location);
	}
}
