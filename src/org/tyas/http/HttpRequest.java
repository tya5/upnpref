package org.tyas.http;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class HttpRequest implements Http.Request
{
	private String mVersion;
	private String mMethod;
	private String mRequestUri;
	private Http.Headers mHeaders;

	public HttpRequest(String version, String method, String requestUri, Http.Headers headers) {
		mVersion = version;
		mMethod = method;
		mRequestUri = requestUri;
		mHeaders = headers;
	}

	public HttpRequest(Http.Message msg) {
	}

	@Override public String getStartLine() {
		return mMethod + " " + mRequestUri + " " + mVersion;
	}

	@Override public Http.Headers getHeaders() {
		return mHeaders;
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

	public static Http.InputRequest parse(InputStream in) throws IOException {
		final Http.InputMessage msg = Http.readMessage(in);

		String start = msg.getStartLine().trim();
		int end = start.indexOf(' ');

		final String method = start.substring(0, end);

		start = start.substring(end + 1).trim();
		end = start.indexOf(' ');

		final String requestUri = start.substring(0, end);
		final String version = start.substring(end + 1).trim();
		
		return new Http.InputRequest() {
			@Override public String getStartLine() { return msg.getStartLine(); }
			@Override public Http.Headers getHeaders() { return msg.getHeaders(); }
			@Override public InputStream getInputStream() { return msg.getInputStream(); }
			@Override public String getVersion() { return version; }
			@Override public String getMethod() { return method; }
			@Override public String getRequestUri() { return requestUri; }
		};
	}

	private void validate() {
		if (mHeaders.getHost() == null) {
			HttpHeaders h = new HttpHeaders(mHeaders);
			h.setHost("");
			mHeaders = h;
		}
	}

	public OutputStream send(OutputStream out) throws IOException {
		validate();
		return Http.writeMessage(out, this, 256);
	}

	public void send(OutputStream out, byte [] entity) throws IOException {
		validate();
		Http.writeMessage(out, this, entity);
	}

	public void send(OutputStream out, InputStream in) throws IOException {
		validate();
		Http.writeMessage(out, this, in);
	}

	public void send(OutputStream out, File f) throws IOException {
		validate();
		Http.writeMessage(out, this, f);
	}
}
