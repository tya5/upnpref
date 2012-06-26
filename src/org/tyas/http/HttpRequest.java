package org.tyas.http;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

public class HttpRequest extends HttpMessage implements Http.Request
{
	private String mVersion;
	private String mMethod;
	private String mRequestUri;

	public HttpRequest(String method, String requestUri, String version) {
		super();
		mVersion = version;
		mMethod = method;
		mRequestUri = requestUri;
	}
	
	public HttpRequest(Http.Message msg) {
		super(msg);

		if (! isValid(msg)) throw new RuntimeException("not HttpRequest");

		String [] start = msg.getStartLine().split(" ", 0);
		mMethod = start[0];
		mRequestUri = start[1];
		mVersion = start[2];
	}
	
	@Override public String getStartLine() {
		return mMethod + " " + mRequestUri + " " + mVersion;
	}

	@Override public String getVersion() {
		return mVersion;
	}

	@Override public String getMethod() {
		return mMethod;
	}

	@Override public String getRequestUri() {
		return mRequestUri;
	}

	@Override protected void validate() {
		if (getHost() == null) {
			setHost("");
		}
	}

	public static boolean isValid(Http.Message msg) {
		String [] start = msg.getStartLine().split(" ", 0);

		if (start.length != 3) return false;

		if (! start[2].startsWith("HTTP/")) return false;

		return true;
	}

	public static Http.InputRequest parse(InputStream in) throws IOException {
		final Http.InputMessage msg = Http.readMessage(in);
		final Http.Request req = new HttpRequest(msg.getMessage());
		
		return new Http.InputRequest() {
			@Override public Http.Request getRequest() { return req; }
			@Override public InputStream getInputStream() { return msg.getInputStream(); }
		};
	}
}
