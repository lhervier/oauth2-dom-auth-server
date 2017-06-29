package com.github.lhervier.domino.oauth.library.server.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.designer.context.XSPContext;

/**
 * Méthodes utiles à l'appli
 * @author Lionel HERVIER
 */
public class XspUtils {

	/**
	 * Pour vérifier si un utilisateur a un rôle. Renvoi un 404 si
	 * ce n'est pas le cas
	 * @param role le role
	 * @throws IOException 
	 */
	public static final void checkRole(String role) throws IOException {
		FacesContext ctx = FacesContext.getCurrentInstance();
		XSPContext xspCtx = (XSPContext) ctx.getApplication().getVariableResolver().resolveVariable(ctx, "context");
		if( xspCtx.getUser().getRoles().contains(role) )
			return;
		
		UIViewRootEx view = (UIViewRootEx) ctx.getApplication().getVariableResolver().resolveVariable(ctx, "view");
		
		OutputStream out = null;
		Writer writer = null;
		try {
			HttpServletResponse resp = (HttpServletResponse) ctx.getExternalContext().getResponse();
			resp.setStatus(404);
			out = resp.getOutputStream();
			writer = new OutputStreamWriter(out, "UTF-8");
			writer.write(
					"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n" +
					"<html>\n" +
					"<head>\n" +
					"<title>Error</title></head>\n" +
					"<body text=\"#000000\">\n" +
					"<h1>Error 404</h1>HTTP Web Server: Item Not Found Exception</body>\n" +
					"</html>"
			);
		} finally {
			IOUtils.closeQuietly(writer);
			IOUtils.closeQuietly(out);
		}
		System.err.println(
				"ATTENTION: L'utilisateur '" + xspCtx.getUser().getFullName() + "' " +
				"a tenté d'accéder à la page '" + view.getPageName() + "'"
		);
		ctx.responseComplete();
	}
}
