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

		String start = msg.getStartLine().trim();
		int end = start.indexOf(' ');

		mMethod = start.substring(0, end);

		start = start.substring(end + 1).trim();
		end = start.indexOf(' ');

		mRequestUri = start.substring(0, end);
		mVersion = start.substring(end + 1).trim();
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

	public static Http.InputRequest parse(InputStream in) throws IOException {
		final Http.InputMessage msg = Http.readMessage(in);
		final Http.Request req = new HttpRequest(msg.getMessage());
		
		return new Http.InputRequest() {
			@Override public Http.Request getRequest() { return req; }
			@Override public InputStream getInputStream() { return msg.getInputStream(); }
		};
	}
}
