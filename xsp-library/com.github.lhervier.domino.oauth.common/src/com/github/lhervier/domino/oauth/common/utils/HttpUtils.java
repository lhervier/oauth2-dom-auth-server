package com.github.lhervier.domino.oauth.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

public class HttpUtils<T, E> {

	/**
	 * L'URL à appeler
	 */
	private String url;
	
	/**
	 * Le type de retour si OK
	 */
	private Class<T> okType;
	
	/**
	 * Le type de retour si KO
	 */
	private Class<E> errorType;
	
	/**
	 * La callback si c'est OK
	 */
	private Callback<T> okCallback;
	
	/**
	 * La callback si c'est KO
	 */
	private Callback<E> errorCallback;
	
	/**
	 * Les headers à ajouter
	 */
	private Map<String, String> headers = new HashMap<String, String>();
	
	/**
	 * Initialise une connection 
	 * @param <T> le type de retour si OK (200)
	 * @param <E> le type de retour si KO
	 * @param url l'url à appeler
	 * @param okType le type de retour si OK
	 * @param errorType le type de retour si KO
	 * @return 
	 */
	public static final <T, E> HttpUtils<T, E> createConnection(String url, Class<T> okType, Class<E> errorType) {
		HttpUtils<T, E> ret = new HttpUtils<T, E>();
		ret.url = url;
		ret.okType = okType;
		ret.errorType = errorType;
		return ret;
	}
	public static final <T, E> HttpUtils<T, E> createConnection(String url) {
		return HttpUtils.createConnection(url, null, null);
	}
	
	/**
	 * Défini la callback quand on reçoit une réponse correct
	 * @param callback la callback
	 * @return
	 */
	public HttpUtils<T, E> onOk(Callback<T> callback) {
		this.okCallback = callback;
		return this;
	}
	
	/**
	 * Défini la callback quand on reçoit une erreur
	 * @param callback la callback
	 * @return
	 */
	public HttpUtils<T, E> onError(Callback<E> callback) {
		this.errorCallback = callback;
		return this;
	}
	
	/**
	 * Ajoute un header
	 * @param name le nom du header
	 * @param value la valeur
	 * @return
	 */
	public HttpUtils<T, E> addHeader(String name, String value) {
		this.headers.put(name, value);
		return this;
	}
	
	/**
	 * Emet la requête
	 * @param obj l'objet à envoyer
	 * @throws IOException 
	 */
	public void execute(Object obj) throws IOException {
		URL url = new URL(this.url);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		InputStream in = null;
		Reader reader = null;
		try {
			// Input et output. Va émettre un GET ou un POST
			conn.setDoInput(this.okCallback != null || this.errorCallback != null);
			conn.setDoOutput(obj != null);
			
			// Ajoute le en tête http
			for( Entry<String, String> entry : this.headers.entrySet() )
				conn.addRequestProperty(entry.getKey(), entry.getValue());
			
			// Charge la réponse (du JSON)
			StringBuffer sb = new StringBuffer();
			if( this.okCallback != null || this.errorCallback != null ) {
				in = conn.getInputStream();
				reader = new InputStreamReader(in, "UTF-8");
				char[] buff = new char[4 * 1024];
				int read = reader.read(buff);
				while( read != -1 ) {
					sb.append(buff, 0, read);
					read = reader.read(buff);
				}
			}
			
			// Code 200 => OK
			if( conn.getResponseCode() == 200 && this.okCallback != null ) {
				T resp = GsonUtils.fromJson(sb.toString(), this.okType);
				this.okCallback.run(resp);
				
			// Code autre => Erreur
			} else if( conn.getResponseCode() != 200 && this.errorCallback != null ) {
				E resp = GsonUtils.fromJson(sb.toString(), this.errorType);
				this.errorCallback.run(resp);
			}
		} finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(in);
			conn.disconnect();
		}
	}
	public void execute() throws IOException {
		this.execute(null);
	}
	
}
