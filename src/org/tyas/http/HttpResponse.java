package org.tyas.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;

public class HttpResponse extends HttpMessage implements Http.Response
{
	private String mVersion;
	private String mStatusCode;
	private String mReasonPhrase;

	public HttpResponse(String version, String statusCode, String reasonPhrase) {
		mVersion = version;
		mStatusCode = statusCode;
		mReasonPhrase = reasonPhrase;
	}

	public HttpResponse(Http.Message msg) {
		super(msg);

		String start = msg.getStartLine().trim();
		int end = start.indexOf(' ');

		mVersion = start.substring(0, end);

		start = start.substring(end + 1).trim();
		end = start.indexOf(' ');

		mStatusCode = start.substring(0, end);
		mReasonPhrase = start.substring(end + 1).trim();
	}

	@Override public String getStartLine() {
		return mVersion + " " + mStatusCode + " " + mReasonPhrase;
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
		final Http.Response resp = new HttpResponse(msg.getMessage());
		
		return new Http.InputResponse() {
			@Override public Http.Response getResponse() { return resp; }
			@Override public InputStream getInputStream() { return msg.getInputStream(); }
		};
	}
}
