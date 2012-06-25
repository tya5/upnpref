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

	public HttpResponse(Http.Message msg) throws IOException {
		super(msg);

		String [] start = msg.getStartLine().split(" ", 0);
		if (start.length >= 3) {
			mVersion = start[0];
			mStatusCode = start[1];
			mReasonPhrase = start[2];
		}

		if ((start.length != 3) || (! mVersion.startsWith("HTTP/"))) {
			throw new HttpMessage.InvalidStartLineException("not HttpResponse");
		}
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
