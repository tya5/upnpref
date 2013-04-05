package org.tyas.upnp.ssdp;

import org.tyas.http.HttpHeaders;
import org.tyas.http.HttpMessage;
import org.tyas.http.HttpRequest;
import org.tyas.http.HttpRequestLine;

import java.io.InputStream;
import java.io.IOException;

public class SsdpSearchRequest extends HttpRequest implements SsdpConstant
{
	private SsdpSearchRequest(HttpRequestLine startLine, HttpHeaders headers) {
		super(startLine, headers);
	}

	public int getMaxWaitTime() { return getInt(MX, -1); }

	public String getSearchTarget() { return getFirst(ST); }

	public String getMan() { return getFirst(MAN); }

	public static SsdpSearchRequest read(InputStream in) throws IOException {
		return HttpMessage.readMessage(in, HttpRequestLine.PARSER, FACTORY).getMessage();
	}

	public static final HttpMessage.Factory<HttpRequestLine,SsdpSearchRequest> FACTORY =
		new HttpMessage.Factory<HttpRequestLine,SsdpSearchRequest>()
	{
		public SsdpSearchRequest createMessage(HttpRequestLine startLine, HttpHeaders headers) {
			if (! M_SEARCH.equals(startLine.getMethod())) return null;

			if (! HttpMessage.VERSION_1_1.equals(startLine.getHttpVersion())) return null;

			return new SsdpSearchRequest(startLine, headers);
		}
	};

	public static class Builder extends HttpRequest.Builder
	{
		private static final HttpRequestLine REQUEST_LINE = new HttpRequestLine(M_SEARCH, "*", HttpMessage.VERSION_1_1);

		public Builder(String host, int port) {
			super(REQUEST_LINE, host, port);
			
			mMap.setFirst(MAN, MAN_DISCOVER);
			setSearchTarget(ST_ALL);
			setMaxWaitTime(1800);
		}

		public Builder() {
			this(MULTICAST_HOST, DEFAULT_PORT);
		}

		public SsdpSearchRequest build() {
			return build(FACTORY);
		}
	
		public void setMaxWaitTime(int seconds) {
			putInt(MX, seconds);
		}

		public void setSearchTarget(String target) {
			mMap.setFirst(ST, target);
		}
	}
}
