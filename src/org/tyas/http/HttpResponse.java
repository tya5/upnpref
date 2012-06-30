package org.tyas.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;

public class HttpResponse extends HttpMessage implements Http.Response
{
	public static class Const extends HttpMessage.Const implements Http.Response
	{
		private String mVersion;
		private String mStatusCode;
		private String mReasonPhrase;

		private Const(HttpMessage.Const c, String v, String s, String r) {
			super(c);
			mVersion = v;
			mStatusCode = s;
			mReasonPhrase = r;
		}

		public Const(Const c) {
			this(c, c.mVersion, c.mStatusCode, c.mReasonPhrase);
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
	}

	public static class Input extends Const
	{
		private InputStream mInput;

		private Input(HttpMessage.Input in, String v, String s, String r) {
			super(in, v, s, r);
			mInput = in.getInputStream();
		}

		public InputStream getInputStream() {
			return mInput;
		}

		public Const toConst() {
			return new Const(this);
		}
	}

	private String mVersion;
	private String mStatusCode;
	private String mReasonPhrase;

	public HttpResponse(String version, String statusCode, String reasonPhrase) {
		super();
		mVersion = version;
		mStatusCode = statusCode;
		mReasonPhrase = reasonPhrase;
	}

	public HttpResponse(AbsHttpMessage msg, String version, String statusCode, String reasonPhrase) {
		super(msg);
		mVersion = version;
		mStatusCode = statusCode;
		mReasonPhrase = reasonPhrase;
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

	private static String [] isValid(AbsHttpMessage msg) {
		String [] start = msg.getStartLine().split(" ", 0);

		if (start.length != 3) return null;

		if (! start[0].startsWith("HTTP/")) return null;

		return start;
	}

	public static HttpResponse getByMessage(AbsHttpMessage msg) {
		String [] s = isValid(msg);

		return s == null ? null: new HttpResponse(msg, s[0], s[1], s[2]);
	}

	public static Const getByMessage(HttpMessage.Const msg) {
		String [] s = isValid(msg);

		return s == null ? null: new Const(msg, s[0], s[1], s[2]);
	}

	public static HttpResponse.Input getByInput(HttpMessage.Input msg) {
		String [] s = isValid(msg);

		return s == null ? null: new Input(msg, s[0], s[1], s[2]);
	}

	public static HttpResponse.Input parse(InputStream in) throws IOException {
		HttpMessage.Input msg = readMessage(in);
		return getByInput(msg);
	}
}
