package org.tyas.upnp.ssdp;

import org.tyas.http.HttpHeaders;
import org.tyas.http.HttpMessage;
import org.tyas.http.HttpRequest;
import org.tyas.http.HttpRequestLine;

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

	public static final HttpMessage.Factory<HttpRequestLine,SsdpSearchRequest> FACTORY =
		new HttpMessage.Factory<HttpRequestLine,SsdpSearchRequest>()
	{
		public SsdpSearchRequest createMessage(HttpRequestLine startLine, HttpHeaders headers) {
			if (! Ssdp.M_SEARCH.equals(startLine.getMethod())) return null;

			if (! HttpMessage.VERSION_1_1.equals(startLine.getHttpVersion())) return null;

			return new SsdpSearchRequest(startLine, headers);
		}
	};

	public static class Builder extends HttpRequest.Builder
	{
		private final HttpRequestLine REQUEST_LINE = new HttpRequestLine(Ssdp.M_SEARCH, "*", HttpMessage.VERSION_1_1);

		public Builder() {
			super.setStartLine(REQUEST_LINE);
		}

		public SsdpSearchRequest build() {
			return build(FACTORY);
		}
	
		public void setMaxWaitTime(int seconds) {
			putInt(Ssdp.MX, seconds);
		}

		public void setSearchTarget(String target) {
			mMap.putFirst(Ssdp.ST, target);
		}

		public void setMan(String man) {
			mMap.putFirst(Ssdp.MAN, man);
		}
	}
}
