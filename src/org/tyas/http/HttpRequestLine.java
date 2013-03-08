package org.tyas.http;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class HttpRequestLine implements HttpStartLine
{
	private static final String REGEXP = "\\s*([!#$%&'*+-.0-9A-Z^_`a-z|~]+)\\s+([^\\s]+)\\s+(HTTP/[0-9]+[.][0-9]+)\\s*";
	private static final Pattern SYNTAX = Pattern.compile(REGEXP);

	private final String mMethod;
	private final String mRequestUri;
	private final String mHttpVersion;

	public HttpRequestLine(String method, String requestUri, String httpVersion) {
		mMethod = method;
		mRequestUri = requestUri;
		mHttpVersion = httpVersion;
	}

	@Override public String getLine() {
		return String.format
			("%s %s %s",
			 mMethod, mRequestUri, mHttpVersion);
	}

	public String getMethod() { return mMethod; }

	public String getRequestUri() { return mRequestUri; }

	public String getHttpVersion() { return mHttpVersion; }

	public static final HttpStartLineParser<HttpRequestLine> PARSER =
		new HttpStartLineParser<HttpRequestLine>()
	{
		public HttpRequestLine parse(String line) {
			Matcher m = SYNTAX.matcher(line);
			String method = null;
			String uri = null;
			String version = null;

			if (m.matches() && m.groupCount() == 3) {
				method = m.group(1);
				uri = m.group(2);
				version = m.group(3);
			} else {
				return null;
			}

			return new HttpRequestLine(method, uri, version);
		}
	};
}
