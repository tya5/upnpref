package org.tyas.http;

import java.io.InputStream;
import java.io.IOException;

public class HttpRequest extends HttpMessage<HttpRequestLine>
{
	protected HttpRequest(HttpRequestLine startLine, HttpHeaders headers) {
		super(startLine, headers);
	}

	public static HttpMessage.Input<HttpRequest> readMessage(InputStream in) throws IOException {
		return HttpMessage.readMessage(in, HttpRequestLine.PARSER, FACTORY);
	}

	public static final HttpMessageFactory<HttpRequestLine,HttpRequest> FACTORY =
		new HttpMessageFactory<HttpRequestLine,HttpRequest>()
	{
		public HttpRequest createMessage(HttpRequestLine startLine, HttpHeaders headers) {
			return new HttpRequest(startLine, headers);
		}
	};
}
