package fr.asso.afer.oauth2.utils;

import java.io.Closeable;
import java.io.IOException;

public class IOUtils {

	/**
	 * Pour fermer un objet
	 * @param o l'objet
	 */
	public static final void closeQuietly(Closeable o) {
		if( o == null )
			return;
		try {
			o.close();
		} catch(IOException e) {
		}
	}
}
