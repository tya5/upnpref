package org.tyas.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;

public class HttpResponse
{
	private String mVersion;
	private String mStatusCode;
	private String mReasonPhrase;

	private HttpResponse(String v, String s, String r) {
		mVersion = v;
		mStatusCode = s;
		mReasonPhrase = r;
	}

	public String getStartLine() {
		return mVersion + " " + mStatusCode + " " + mReasonPhrase;
	}

	public String getVersion() { return mVersion; }

	public String getStatusCode() { return mStatusCode; }

	public String getReasonPhrase() { return mReasonPhrase; }

	public interface Base extends HttpMessage.Base
	{
		String getVersion();
		String getStatusCode();
		String getReasonPhrase();
	}
	
	public static class Const extends HttpMessage.Const implements Base
	{
		private HttpResponse mResp;

		private Const(String v, String s, String r, HttpMessage.Const c) {
			super(c);
			mResp = new HttpResponse(v, s, r);
		}

		public Const(HttpResponse.Const c) {
			this(c.getVersion(), c.getStatusCode(), c.getReasonPhrase(), c);
		}

		@Override public String getStartLine() { return mResp.getStartLine(); }
		@Override public String getVersion() { return mResp.getVersion(); }
		@Override public String getStatusCode() { return mResp.getStatusCode(); }
		@Override public String getReasonPhrase() { return mResp.getReasonPhrase(); }
	}

	public static class Input extends Const
	{
		private InputStream mInput;

		private Input(String v, String s, String r, HttpMessage.Input in) {
			super(v, s, r, in);
			mInput = in.getInputStream();
		}

		public InputStream getInputStream() { return mInput; }
	}

	public static class Builder extends HttpMessage.Builder implements Base
	{
		private HttpResponse mResp;

		public Builder(String version, String statusCode, String reasonPhrase) {
			super();
			mResp = new HttpResponse(version, statusCode, reasonPhrase);
		}

		public Builder(String version, String statusCode, String reasonPhrase, HttpMessage msg) {
			super(msg);
			mResp = new HttpResponse(version, statusCode, reasonPhrase);
		}

		@Override public String getStartLine() { return mResp.getStartLine(); }
		@Override public String getVersion() { return mResp.getVersion(); }
		@Override public String getStatusCode() { return mResp.getStatusCode(); }
		@Override public String getReasonPhrase() { return mResp.getReasonPhrase(); }
	}

	private static String [] isValid(HttpMessage msg) {
		String [] start = msg.getStartLine().split(" ", 0);

		if (start.length != 3) return null;

		if (! start[0].startsWith("HTTP/")) return null;

		return start;
	}

	public static Builder getByMessage(HttpMessage msg) {
		String [] s = isValid(msg);

		return s == null ? null: new Builder(s[0], s[1], s[2], msg);
	}

	public static Const getByMessage(HttpMessage.Const msg) {
		String [] s = isValid(msg);

		return s == null ? null: new Const(s[0], s[1], s[2], msg);
	}

	public static Input getByInput(HttpMessage.Input msg) {
		String [] s = isValid(msg);

		return s == null ? null: new Input(s[0], s[1], s[2], msg);
	}

	public static Input parse(InputStream in) throws IOException {
		HttpMessage.Input msg = HttpMessage.readMessage(in);
		return getByInput(msg);
	}
}
