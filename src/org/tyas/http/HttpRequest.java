package org.tyas.http;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

public class HttpRequest
{
	private String mMethod;
	private String mRequestUri;
	private String mVersion;

	private HttpRequest(String method, String uri, String version) {
		mMethod = method;
		mRequestUri = uri;
		mVersion = version;
	}

	public String getStartLine() {
		return mMethod + " " + mRequestUri + " " + mVersion;
	}

	public String getVersion() {
		return mVersion;
	}

	public String getMethod() {
		return mMethod;
	}

	public String getRequestUri() {
		return mRequestUri;
	}

	public interface Base extends HttpMessage.Base
	{
		String getVersion();
		String getMethod();
		String getRequestUri();
	}

	public static class Const extends HttpMessage.Const implements Base
	{
		private HttpRequest mReq;

		private Const(String m, String r, String v, HttpMessage.Const c) {
			super(c);
			mReq = new HttpRequest(m, r, v);
		}

		public Const(HttpRequest.Const c) {
			this(c.getMethod(), c.getRequestUri(), c.getVersion(), c);
		}

		@Override public String getStartLine() { return mReq.getStartLine(); }
		@Override public String getMethod() { return mReq.getMethod(); }
		@Override public String getRequestUri() { return mReq.getRequestUri(); }
		@Override public String getVersion() { return mReq.getVersion(); }
	}

	public static class Input extends Const
	{
		private InputStream mInput;

		private Input(String m, String r, String v, HttpMessage.Input in) {
			super(m, r, v, in);
			mInput = in.getInputStream();
		}

		public InputStream getInputStream() { return mInput; }
	}

	public static class Builder extends HttpMessage.Builder implements Base
	{
		private HttpRequest mReq;

		public Builder(String method, String requestUri, String version) {
			super();
			mReq = new HttpRequest(method, requestUri, version);
		}
	
		private Builder(String method, String requestUri, String version, HttpMessage msg) {
			super(msg);
			mReq = new HttpRequest(method, requestUri, version);
		}

		@Override protected void validate() {
			if (getHost() == null) {
				setHost("");
			}
		}

		@Override public String getStartLine() { return mReq.getStartLine(); }
		@Override public String getMethod() { return mReq.getMethod(); }
		@Override public String getRequestUri() { return mReq.getRequestUri(); }
		@Override public String getVersion() { return mReq.getVersion(); }
	}

	private static String [] isValid(HttpMessage msg) {
		String [] start = msg.getStartLine().split(" ", 0);

		if (start.length != 3) return null;

		if (! start[2].startsWith("HTTP/")) return null;

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

	public static HttpRequest.Input parse(InputStream in) throws IOException {
		HttpMessage.Input msg = HttpMessage.readMessage(in);
		return getByInput(msg);
	}
}
