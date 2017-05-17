package fr.asso.afer.oauth2.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

/**
 * Méthodes utiles à l'appli
 * @author Lionel HERVIER
 */
public class Utils {

	/**
	 * Retourne la base carnet d'adresse.
	 * @param session la session Notes
	 * @return le NAB
	 * @throws NotesException en cas de pb
	 */
	public static final Database getNab(Session session) throws NotesException {
		Database names = DominoUtils.openDatabase(
				session, 
				JSFUtils.getParamsBean().getNab()
		);
		if( names == null )
			throw new RuntimeException("Je n'arrive pas à ouvrir la base '" + JSFUtils.getParamsBean().getNab() + "'");
		return names;
	}
	
	/**
	 * Pour vérifier si un utilisateur a un rôle. Renvoi un 404 si
	 * ce n'est pas le cas
	 * @param role le role
	 * @throws IOException 
	 */
	public static final void checkRole(String role) throws IOException {
		if( !JSFUtils.getContext().getUser().getRoles().contains(role) )
			send404();
	}
	
	/**
	 * Renvoi une erreur 404
	 * @throws IOException 
	 */
	public static final void send404() throws IOException {
		OutputStream out = null;
		Writer writer = null;
		try {
			HttpServletResponse resp = JSFUtils.getServletResponse();
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
				"ATTENTION: L'utilisateur '" + JSFUtils.getContext().getUser().getFullName() + "' " +
				"a tenté d'accéder à la page '" +JSFUtils.getView().getPageName() + "'"
		);
		FacesContext.getCurrentInstance().responseComplete();
	}
}
