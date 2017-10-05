package com.github.lhervier.domino.oauth.client.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class HttpConnection<T, E> {

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
	 * Un éventuel hostname verifier (si connection https)
	 */
	private HostnameVerifier verifier = null;
	
	/**
	 * Un éventuel SSLFactory (si connection https)
	 */
	private SSLSocketFactory factory = null;
	
	/**
	 * Le contenu à envoyer
	 */
	private InputStream content;
	
	/**
	 * Jackson mapper
	 */
	private ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * Initialise une connection 
	 * @param <T> le type de retour si OK (200)
	 * @param <E> le type de retour si KO
	 * @param url l'url à appeler
	 * @param okType le type de retour si OK
	 * @param errorType le type de retour si KO
	 * @return 
	 */
	public static final <T, E> HttpConnection<T, E> createConnection(String url, Class<T> okType, Class<E> errorType) {
		HttpConnection<T, E> ret = new HttpConnection<T, E>();
		ret.url = url;
		ret.okType = okType;
		ret.errorType = errorType;
		return ret;
	}
	public static final <T, E> HttpConnection<T, E> createConnection(String url) {
		return HttpConnection.createConnection(url, null, null);
	}
	
	/**
	 * Défini la callback quand on reçoit une réponse correct
	 * @param callback la callback
	 * @return
	 */
	public HttpConnection<T, E> onOk(Callback<T> callback) {
		this.okCallback = callback;
		return this;
	}
	
	/**
	 * Défini la callback quand on reçoit une erreur
	 * @param callback la callback
	 * @return
	 */
	public HttpConnection<T, E> onError(Callback<E> callback) {
		this.errorCallback = callback;
		return this;
	}
	
	/**
	 * Ajoute un header
	 * @param name le nom du header
	 * @param value la valeur
	 * @return
	 */
	public HttpConnection<T, E> addHeader(String name, String value) {
		this.headers.put(name, value);
		return this;
	}
	
	/**
	 * Pour ajouter un verifier
	 * @param verifier le verifier
	 */
	public HttpConnection<T, E> withVerifier(HostnameVerifier verifier) {
		this.verifier = verifier;
		return this;
	}
	
	/**
	 * Pour ajouter un SSLFactory
	 * @param factory la factory
	 */
	public HttpConnection<T, E> withFactory(SSLSocketFactory factory) {
		this.factory = factory;
		return this;
	}
	
	/**
	 * Pour définir un contenu texte
	 * @param content le contenu
	 * @param encoding l'encodage à utiliser
	 * @throws UnsupportedEncodingException 
	 */
	public HttpConnection<T, E> setTextContent(String content, String encoding) throws UnsupportedEncodingException {
		this.content = new ByteArrayInputStream(content.getBytes(encoding));
		return this;
	}
	
	/**
	 * Pour définir un contenu objet à serialiser en Json
	 * @param content le contenu
	 * @param encoding l'encodage
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	public HttpConnection<T, E> setJsonContent(Object content, String encoding) throws JsonGenerationException, JsonMappingException, IOException {
		this.content = new ByteArrayInputStream(this.mapper.writeValueAsString(content).getBytes(encoding));
		return this;
	}
	
	/**
	 * Pour définir un contenu
	 * @param content le contenu
	 */
	public HttpConnection<T, E> setContent(byte[] content) {
		this.content = new ByteArrayInputStream(content);
		return this;
	}
	
	/**
	 * Pour définir un contenu
	 * @param in stream vers le contenu
	 */
	public HttpConnection<T, E> setContent(InputStream in) {
		this.content = in;
		return this;
	}
	
	/**
	 * Emet la requête
	 * @throws IOException 
	 */
	public void execute() throws IOException {
		URL url = new URL(this.url);
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();;
		if( "https".equals(url.getProtocol()) ) {
			HttpsURLConnection conns = (HttpsURLConnection) conn;
			if( this.verifier != null )
				conns.setHostnameVerifier(this.verifier);
			
			if( this.factory != null )
				conns.setSSLSocketFactory(this.factory);
		}
		
		InputStream in = null;
		Reader reader = null;
		try {
			// Input et output. Va émettre un GET ou un POST
			conn.setDoInput(this.okCallback != null || this.errorCallback != null);
			conn.setDoOutput(this.content != null);
			
			// Ajoute les en têtes http
			for( Entry<String, String> entry : this.headers.entrySet() )
				conn.addRequestProperty(entry.getKey(), entry.getValue());
			
			// Envoi l'objet
			if( this.content != null ) {
				OutputStream out = null;
				try {
					out = conn.getOutputStream();
					IOUtils.copy(this.content, out);
				} finally {
					IOUtils.closeQuietly(out);
					IOUtils.closeQuietly(this.content);
				}
			}
			
			// Charge la réponse (du JSON)
			StringBuffer sb = new StringBuffer();
			
			// Stream pour accéder au contenu (en fonction d'une erreur)
			if( conn.getResponseCode() == 200 )
				in = conn.getInputStream();
			else
				in = conn.getErrorStream();
			
			// Lit le contenu de la réponse
			reader = new InputStreamReader(in, "UTF-8");
			char[] buff = new char[4 * 1024];
			int read = reader.read(buff);
			while( read != -1 ) {
				sb.append(buff, 0, read);
				read = reader.read(buff);
			}
			
			// Code 200 => OK
			if( conn.getResponseCode() == 200 && this.okCallback != null ) {
				T resp = this.mapper.readValue(sb.toString(), this.okType);
				this.okCallback.run(resp);
				
			// Code autre => Erreur
			} else if( conn.getResponseCode() != 200 && this.errorCallback != null ) {
				E resp = this.mapper.readValue(sb.toString(), this.errorType);
				this.errorCallback.run(resp);
			}
		} catch(IOException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(in);
			conn.disconnect();
		}
	}
}
