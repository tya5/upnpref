package org.tyas.upnp.ssdp;

import org.tyas.http.HttpHeaders;
import org.tyas.http.HttpMessage;
import org.tyas.http.HttpResponse;
import org.tyas.http.HttpStatusLine;
import org.tyas.upnp.UpnpUsn;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SsdpSearchResponse extends HttpResponse
	implements SsdpConstant
{
	private SsdpSearchResponse(HttpStatusLine startLine, HttpHeaders headers) {
		super(startLine, headers);
	}

	public URL getDescriptionUrl() {
		try {
			return getLocation().toURL();
		} catch (Exception e) {
			return null;
		}
	}

	public String getSearchTarget() { return getFirst(ST); }

	public UpnpUsn getUniqueServiceName() { return UpnpUsn.getByString(getFirst(USN)); }

	public int getBootId() { return getInt(BOOTID, 0); }

	public int getConfigId() { return getInt(CONFIGID, 0); }

	public int getSearchPort() { return getInt(SEARCHPORT, -1); }

	public static SsdpSearchResponse read(InputStream in) throws IOException {
		return HttpMessage.readMessage(in, HttpStatusLine.PARSER, FACTORY).getMessage();
	}

	public static final HttpMessage.Factory<HttpStatusLine,SsdpSearchResponse> FACTORY =
		new HttpMessage.Factory<HttpStatusLine,SsdpSearchResponse>()
	{
		public SsdpSearchResponse createMessage(HttpStatusLine startLine, HttpHeaders headers) {
			return new SsdpSearchResponse(startLine, headers);
		}
	};

	public static class Builder extends HttpResponse.Builder
	{
		public Builder() {
			this(HttpStatusLine.DEFAULT_200_OK);
			putMaxAge(1800);
		}

		public Builder(HttpStatusLine statusLine) {
			super(statusLine);
		}

		public SsdpSearchResponse build() {
			return build(FACTORY);
		}

		public void setDescriptionUrl(String url) {
			setLocation(url);
		}

		public void setSearchTarget(String target) {
			mMap.setFirst(ST, target);
		}

		public void setUniqueServiceName(String usn) {
			mMap.setFirst(USN, usn);
		}
	
		public void setBootId(int id) {
			putInt(BOOTID, id);
		}

		public void setConfigId(int id) {
			putInt(CONFIGID, id);
		}

		public void setSearchPort(int port) {
			putInt(SEARCHPORT, port);
		}
	}
}
