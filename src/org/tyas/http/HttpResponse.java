package org.tyas.http;

import java.io.InputStream;
import java.io.IOException;

public class HttpResponse extends HttpMessage<HttpStatusLine>
{
	protected HttpResponse(HttpStatusLine startLine, HttpHeaders headers) {
		super(startLine, headers);
	}

	public static HttpMessage.Input<HttpResponse> readMessage(InputStream in) throws IOException {
		return HttpMessage.readMessage(in, HttpStatusLine.PARSER, FACTORY);
	}

	public static final HttpMessageFactory<HttpStatusLine,HttpResponse> FACTORY =
		new HttpMessageFactory<HttpStatusLine,HttpResponse>()
		{
			public HttpResponse createMessage(HttpStatusLine startLine, HttpHeaders headers) {
				return new HttpResponse(startLine, headers);
			}
		};

	public static final HttpResponse RESPONSE_OK =
		new HttpMessage.Builder<HttpStatusLine>
		(new HttpStatusLine(HttpMessage.VERSION_1_1, "200", "OK"))
		.build(FACTORY);
}
