package org.tyas.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;

public class HttpResponse implements Http.Response
{
	private String mVersion;
	private String mStatusCode;
	private String mReasonPhrase;
	private Http.Headers mHeaders;

	public HttpResponse(String version, String statusCode, String reasonPhrase, Http.Headers headers) {
		mVersion = version;
		mStatusCode = statusCode;
		mReasonPhrase = reasonPhrase;
		mHeaders = headers;
	}

	@Override public String getStartLine() {
		return mVersion + " " + mStatusCode + " " + mReasonPhrase;
	}

	@Override public Http.Headers getHeaders() {
		return mHeaders;
	}

	@Override public String getVersion() {
		return mVersion;
	}

	@Override public String getStatusCode() {
		return mStatusCode;
	}

	@Override public String getReasonPhrase() {
		return mReasonPhrase;
	}

	public static Http.InputResponse parse(InputStream in) throws IOException {
		final Http.InputMessage msg = Http.readMessage(in);

		String start = msg.getStartLine().trim();
		int end = start.indexOf(' ');

		final String version = start.substring(0, end);

		start = start.substring(end + 1).trim();
		end = start.indexOf(' ');

		final String statusCode = start.substring(0, end);
		final String reasonPhrase = start.substring(end + 1).trim();
		
		return new Http.InputResponse() {
			@Override public String getStartLine() { return msg.getStartLine(); }
			@Override public Http.Headers getHeaders() { return msg.getHeaders(); }
			@Override public InputStream getInputStream() { return msg.getInputStream(); }
			@Override public String getVersion() { return version; }
			@Override public String getStatusCode() { return statusCode; }
			@Override public String getReasonPhrase() { return reasonPhrase; }
		};
	}

	public OutputStream send(OutputStream out) throws IOException {
		return Http.writeMessage(out, this, 256);
	}

	public void send(OutputStream out, byte [] entity) throws IOException {
		Http.writeMessage(out, this, entity);
	}

	public void send(OutputStream out, InputStream in) throws IOException {
		Http.writeMessage(out, this, in);
	}

	public void send(OutputStream out, File f) throws IOException {
		Http.writeMessage(out, this, f);
	}
}
