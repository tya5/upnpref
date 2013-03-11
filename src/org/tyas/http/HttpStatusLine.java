package org.tyas.http;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class HttpStatusLine implements HttpStartLine
{
	public static final HttpStatusLine DEFAULT_200_OK = new HttpStatusLine(HttpMessage.VERSION_1_1, "200", "OK");

	private static final String REGEXP = "\\s*(HTTP/[0-9]+[.][0-9]+)\\s+([0-9]{3}+)\\s([^\r\n]*)";
	private static final Pattern SYNTAX = Pattern.compile(REGEXP);

	private final String mHttpVersion;
	private final String mStatusCode;
	private final String mReasonPhrase;

	public HttpStatusLine(String version, String statusCode, String reasonPhrase) {
		mHttpVersion = version;
		mStatusCode = statusCode;
		mReasonPhrase = reasonPhrase;
	}

	@Override public String getLine() {
		return String.format
			("%s %s %s",
			 mHttpVersion, mStatusCode, mReasonPhrase);
	}

	public String getHttpVersion() { return mHttpVersion; }

	public String getStatusCode() { return mStatusCode; }

	public String getReasonPhrase() { return mReasonPhrase; }

	public static final HttpStartLine.Parser<HttpStatusLine> PARSER =
		new HttpStartLine.Parser<HttpStatusLine>()
	{
		public HttpStatusLine parse(String line) {
			Matcher m = SYNTAX.matcher(line);
			String version = null;
			String status = null;
			String reason = null;

			if (m.matches() && m.groupCount() == 3) {
				version = m.group(1);
				status = m.group(2);
				reason = m.group(3);
			} else {
				return null;
			}

			return new HttpStatusLine(version, status, reason);
		}
	};
}
