package bwfdm.connector.dspace.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for I/O operations
 * 
 * @author Markus Gärtner
 *
 */
public class IOUtils {

	private static final Logger log = LoggerFactory.getLogger(IOUtils.class);
	
	public static String readStream(InputStream input) throws IOException {
		return readStream(input, StandardCharsets.UTF_8);
	}

	public static String readStream(InputStream input, Charset encoding) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copyStream(input, baos);
		input.close();
		return new String(baos.toByteArray(), encoding);
	}

	public static String readStreamUnchecked(InputStream input) {
		return readStreamUnchecked(input, StandardCharsets.UTF_8);
	}

	public static String readStreamUnchecked(InputStream input, Charset encoding) {
		try {
			return readStream(input, encoding);
		} catch (IOException e) {
			// ignore
		}

		return null;
	}
	
	public static void copyStream(final InputStream in, final OutputStream out) throws IOException {
    	copyStream(in, out, 0);
    }

    public static void copyStream(final InputStream in, final OutputStream out,
            int bufferSize) throws IOException {
    	if(bufferSize==0) {
    		bufferSize = 8000;
    	}
        byte[] buf = new byte[bufferSize];
        int len;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
    }
	
	public static void closeQuietly(Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException e) {
			log.error("Failed to close resource", e);
		}
	}
	
}
