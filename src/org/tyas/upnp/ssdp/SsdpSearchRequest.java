package org.tyas.upnp.ssdp;

import org.tyas.http.HttpHeaders;
import org.tyas.http.HttpMessage;
import org.tyas.http.HttpRequest;
import org.tyas.http.HttpRequestLine;
import org.tyas.http.HttpMessageFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

public class SsdpSearchRequest extends HttpRequest
{
	private SsdpSearchRequest(HttpRequestLine startLine, HttpHeaders headers) {
		super(startLine, headers);
	}

	public int getMaxWaitTime() { return getInt(Ssdp.MX, -1); }

	public String getSearchTarget() { return getFirst(Ssdp.ST); }

	public String getMan() { return getFirst(Ssdp.MAN); }

	public static SsdpSearchRequest read(InputStream in) throws IOException {
		return HttpMessage.readMessage(in, HttpRequestLine.PARSER, FACTORY).getMessage();
	}

	public static final HttpMessageFactory<HttpRequestLine,SsdpSearchRequest> FACTORY =
		new HttpMessageFactory<HttpRequestLine,SsdpSearchRequest>()
	{
		public SsdpSearchRequest createMessage(HttpRequestLine startLine, HttpHeaders headers) {
			if (! Ssdp.M_SEARCH.equals(startLine.getMethod())) return null;

			if (! HttpMessage.VERSION_1_1.equals(startLine.getHttpVersion())) return null;

			return new SsdpSearchRequest(startLine, headers);
		}
	};

	public static class Builder
	{
		public final HttpMessage.Builder<HttpRequestLine> mHttpMessageBuilder =
			new HttpMessage.Builder<HttpRequestLine>
			(new HttpRequestLine(Ssdp.M_SEARCH, "*", HttpMessage.VERSION_1_1));

		public SsdpSearchRequest build() {
			return mHttpMessageBuilder.build(FACTORY);
		}
	
		public Builder setMaxWaitTime(int seconds) {
			mHttpMessageBuilder.setInt(Ssdp.MX, seconds);
			return this;
		}

		public Builder setSearchTarget(String target) {
			mHttpMessageBuilder.putFirst(Ssdp.ST, target);
			return this;
		}

		public Builder setMan(String man) {
			mHttpMessageBuilder.putFirst(Ssdp.MAN, man);
			return this;
		}
	}
}
