package org.tyas.http;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

public class HttpRequest extends HttpMessage implements Http.Request
{
	public static class Const extends HttpMessage.Const implements Http.Request
	{
		private String mVersion;
		private String mMethod;
		private String mRequestUri;

		private Const(HttpMessage.Const c, String m, String r, String v) {
			super(c);
			mVersion = v;
			mMethod = m;
			mRequestUri = r;
		}

		public Const(Const c) {
			this(c, c.mMethod, c.mRequestUri, c.mVersion);
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
	}

	public static class Input extends Const
	{
		private InputStream mInput;

		private Input(HttpMessage.Input in, String m, String r, String v) {
			super(in, m, r, v);
			mInput = in.getInputStream();
		}

		public InputStream getInputStream() {
			return mInput;
		}

		public Const toConst() {
			validate();
			return new Const(this);
		}
	}
	
	private String mVersion;
	private String mMethod;
	private String mRequestUri;

	public HttpRequest(String method, String requestUri, String version) {
		super();
		mVersion = version;
		mMethod = method;
		mRequestUri = requestUri;
	}
	
	private HttpRequest(AbsHttpMessage msg, String method, String requestUri, String version) {
		super(msg);
		mVersion = version;
		mMethod = method;
		mRequestUri = requestUri;
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

	private static String [] isValid(AbsHttpMessage msg) {
		String [] start = msg.getStartLine().split(" ", 0);

		if (start.length != 3) return null;

		if (! start[2].startsWith("HTTP/")) return null;

		return start;
	}

	public static HttpRequest getByMessage(AbsHttpMessage msg) {
		String [] s = isValid(msg);

		return s == null ? null: new HttpRequest(msg, s[0], s[1], s[2]);
	}

	public static Const getByMessage(HttpMessage.Const msg) {
		String [] s = isValid(msg);

		return s == null ? null: new Const(msg, s[0], s[1], s[2]);
	}

	public static HttpRequest.Input getByInput(HttpMessage.Input msg) {
		String [] s = isValid(msg);

		return s == null ? null: new Input(msg, s[0], s[1], s[2]);
	}

	public static HttpRequest.Input parse(InputStream in) throws IOException {
		HttpMessage.Input msg = readMessage(in);
		return getByInput(msg);
	}
}
