package org.tyas.http;

import java.io.InputStream;
import java.io.IOException;

public class HttpRequest extends HttpMessage<HttpRequestLine>
{
	public static final String HOST = "HOST";

	protected HttpRequest(HttpRequestLine startLine, HttpHeaders headers) {
		super(startLine, headers);
	}

	public String getHost() {
		String host = getFirst(HOST);

		if (host == null) return null;

		try {
			return host.split(":")[0];
		} catch (Exception e) {
			return "";
		}
	}

	public int getPort() {
		try {
			return Integer.decode(getFirst(HOST).split(":")[1]);
		} catch (Exception e) {
			return -1;
		}
	}

	public static HttpMessage.Input<HttpRequest> readMessage(InputStream in) throws IOException {
		return HttpRequest.readMessage(in, FACTORY);
	}

	public static <M extends HttpRequest> HttpMessage.Input<M> readMessage
		(InputStream in, HttpMessage.Factory<HttpRequestLine,M> factory) throws IOException {
		return HttpMessage.readMessage(in, HttpRequestLine.PARSER, factory);
	}

	public static final HttpMessage.Factory<HttpRequestLine,HttpRequest> FACTORY =
		new HttpMessage.Factory<HttpRequestLine,HttpRequest>()
	{
		public HttpRequest createMessage(HttpRequestLine startLine, HttpHeaders headers) {
			return new HttpRequest(startLine, headers);
		}
	};

	public static class Builder extends HttpMessage.Builder<HttpRequestLine>
	{
		public HttpRequest build() {
			return build(FACTORY);
		}

		public void putHost(String host, int port) {
			if (port < 0) {
				mMap.putFirst(HOST, host);
			} else {
				mMap.putFirst(HOST, host + ":" + port);
			}
		}

		public void putHost(String host) {
			putHost(host, -1);
		}
	}
}
