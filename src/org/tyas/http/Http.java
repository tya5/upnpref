package org.tyas.http;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;
import java.net.DatagramPacket;

public class Http
{
	public static final char LF = '\n';
	public static final char CR = '\r';
	public static final String CRLF = "\r\n";

	public static final String VERSION_1_0 = "HTTP/1.0";
	public static final String VERSION_1_1 = "HTTP/1.1";

	public static final String HOST = "HOST";
	public static final String LOCATION = "LOCATION";
	public static final String TRANSFER_ENCODING = "TRANSFER-ENCODING";
	public static final String CONTENT_LENGTH = "CONTENT-LENGTH";
	public static final String SERVER = "SERVER";
	public static final String CACHE_CONTROL = "CACHE-CONTROL";
	public static final String CONNECTION = "CONNECTION";
	public static final String KEEP_ALIVE = "KEEP-ALIVE";
	public static final String CLOSE = "CLOSE";
	public static final String CHUNKED = "chunked";
	public static final String MAX_AGE = "max-age";

	public interface Message
	{
		String getStartLine();
		Set<String> keySet();
		List<String> get(String name);
		String getFirst(String name);
		int getInt(String name, int defaultValue);
		long getLong(String name, long defaultValue);
		boolean isKeepAlive();
		boolean isChunkedEncoding();
		long getContentLength();
		String getHost();
		int getPort();
		long getMaxAge();
		URI getLocation();
		OutputStream send(OutputStream out) throws IOException;
		void send(OutputStream out, byte [] entity) throws IOException;
		void send(OutputStream out, InputStream in) throws IOException;
		void send(OutputStream out, File f) throws IOException;
		DatagramPacket toDatagramPacket() throws IOException;
	}

	public interface Request extends Message
	{
		String getVersion();
		String getMethod();
		String getRequestUri();
	}

	public interface Response extends Message
	{
		String getVersion();
		String getStatusCode();
		String getReasonPhrase();
	}
}
