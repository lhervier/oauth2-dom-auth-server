package com.github.lhervier.domino.oauth.client.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

	public static final void closeQuietly(Closeable obj) {
		if( obj == null )
			return;
		try {
			obj.close();
		} catch(IOException e) {
			e.printStackTrace(System.err);
		}
	}
	
	public static final void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[4*1024];
		int read = in.read(buffer);
		while( read != -1 ) {
			out.write(buffer, 0, read);
			read = in.read(buffer);
		}
	}
}
