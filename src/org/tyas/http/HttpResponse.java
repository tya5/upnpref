package org.tyas.http;

import java.net.URI;
import java.io.InputStream;
import java.io.IOException;

public class HttpResponse extends HttpMessage<HttpStatusLine>
{
	public static final String LOCATION = "LOCATION";

	protected HttpResponse(HttpStatusLine startLine, HttpHeaders headers) {
		super(startLine, headers);
	}

	public URI getLocation() {
		try {
			return new URI(getFirst(LOCATION));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static HttpMessage.Input<HttpResponse> readMessage(InputStream in) throws IOException {
		return HttpResponse.readMessage(in, FACTORY);
	}

	public static <M extends HttpResponse> HttpMessage.Input<M> readMessage
		(InputStream in, HttpMessage.Factory<HttpStatusLine,M> factory) throws IOException {
		return HttpMessage.readMessage(in, HttpStatusLine.PARSER, factory);
	}

	public static final HttpMessage.Factory<HttpStatusLine,HttpResponse> FACTORY =
		new HttpMessage.Factory<HttpStatusLine,HttpResponse>()
		{
			public HttpResponse createMessage(HttpStatusLine startLine, HttpHeaders headers) {
				return new HttpResponse(startLine, headers);
			}
		};

	public static class Builder extends HttpMessage.Builder<HttpStatusLine>
	{
		public HttpResponse build() {
			return build(FACTORY);
		}

		public void setLocation(String uri) {
			mMap.putFirst(LOCATION, uri);
		}

		public void setLocation(URI uri) {
			setLocation(uri.toString());
		}
	}
}
